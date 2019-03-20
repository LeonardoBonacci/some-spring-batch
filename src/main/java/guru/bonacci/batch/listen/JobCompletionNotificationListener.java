package guru.bonacci.batch.listen;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import guru.bonacci.batch.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

	@Value("${file.path.errors:errors.txt}")
	private String fileName;

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void afterJob(JobExecution jobExecution) {
		if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
			log.info("Well done. Time to verify the results. Invalid transactions are written to " + fileName);

			jdbcTemplate
					.query("SELECT reference, account_number, description, start_balance, mutation, end_balance  FROM transactions",
							(rs, row) -> new Transaction(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getBigDecimal(4), rs.getBigDecimal(5), rs.getBigDecimal(6)))
					.forEach(tx -> log.info("Successfully processed <" + tx + "> in the database."));
		}
	}
}
