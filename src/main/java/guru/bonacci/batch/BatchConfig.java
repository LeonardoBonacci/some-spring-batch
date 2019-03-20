package guru.bonacci.batch;

import java.util.Arrays;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import guru.bonacci.batch.listen.FileAppendingSkipListener;
import guru.bonacci.batch.listen.JobCompletionNotificationListener;
import guru.bonacci.batch.model.Transaction;
import guru.bonacci.batch.validation.EndBalanceValidatingItemProcessor;
import guru.bonacci.batch.validation.ReferenceValidatingItemProcessor;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private EndBalanceValidatingItemProcessor balanceValidator;

	@Autowired
	private ReferenceValidatingItemProcessor referenceValidator;
	

	// For reading
	@Bean
	protected FlatFileItemReader<Transaction> csvReader() {
		return new FlatFileItemReaderBuilder<Transaction>().name("transactionItemReader")
				.resource(new ClassPathResource("records.csv")).linesToSkip(1).delimited().names(new String[] {
						"reference", "accountNumber", "description", "startBalance", "mutation", "endBalance" })
				.fieldSetMapper(new BeanWrapperFieldSetMapper<Transaction>() {
					{
						setTargetType(Transaction.class);
					}
				}).build();
	}

	@Bean
	protected Jaxb2Marshaller marshaller() {
		Jaxb2Marshaller unmarshaller = new Jaxb2Marshaller();
		unmarshaller.setClassesToBeBound(Transaction.class);
		return unmarshaller;
	}

	@Bean
	protected StaxEventItemReader<Transaction> xmlReader() {
		StaxEventItemReader<Transaction> reader = new StaxEventItemReader<>();
		reader.setResource(new ClassPathResource("records.xml"));
		reader.setFragmentRootElementName("record");
		reader.setUnmarshaller(marshaller());
		return reader;
	}


	// For processing
	@Bean
	protected ItemProcessor<Transaction, Transaction> processor() {
	    final CompositeItemProcessor<Transaction, Transaction> processor = new CompositeItemProcessor<>();
	    processor.setDelegates(Arrays.asList(balanceValidator, referenceValidator));
	    return processor;
	}
	
	
	// For writing
	@Bean
	public JdbcBatchItemWriter<Transaction> writer(DataSource dataSource) {
		return new JdbcBatchItemWriterBuilder<Transaction>()
				.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
				.sql("INSERT INTO transactions (reference, account_number, description, start_balance, mutation, end_balance) "
						+ "VALUES (:reference, :accountNumber, :description, :startBalance, :mutation, :endBalance)")
				.dataSource(dataSource).build();
	}

	
	// For job execution
	@Bean
	public Job xmlJob(JobCompletionNotificationListener listener, Step xmlStep) {
		return jobBuilderFactory.get("xmlJob")
				.listener(listener)
				.flow(xmlStep)
				.end()
				.build();
	}

	//TODO remove code duplication. Same as csvStep. Do something fancy with tasklets.
	@Bean
	protected Step xmlStep(FileAppendingSkipListener listener, JdbcBatchItemWriter<Transaction> writer) {
		return stepBuilderFactory.get("xml").<Transaction, Transaction>chunk(5)
					 .reader(csvReader())
					 .processor(processor())
					 .faultTolerant()
					 .skip(ValidationException.class)
					 .skipLimit(1000) // whatever seems appropriate here.. 
					 .listener(listener)
					 .writer(writer)
					 .build();
	}

	@Bean
	public Job csvJob(JobCompletionNotificationListener listener, Step csvStep) {
		return jobBuilderFactory.get("csvJob")
				.listener(listener)
				.flow(csvStep)
				.end()
				.build();
	}

	//TODO remove code duplication. Same as xmlStep. Do something fancy with tasklets.
	@Bean
	protected Step csvStep(FileAppendingSkipListener listener, JdbcBatchItemWriter<Transaction> writer) {
		return stepBuilderFactory.get("csv").<Transaction, Transaction>chunk(5)
					 .reader(csvReader())
					 .processor(processor())
					 .faultTolerant()
					 .skip(ValidationException.class)
					 .skipLimit(1000) // whatever seems appropriate here.. 
					 .listener(listener)
					 .writer(writer)
					 .build();
	}
}
