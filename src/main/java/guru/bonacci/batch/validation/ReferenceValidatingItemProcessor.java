package guru.bonacci.batch.validation;

import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.stereotype.Component;

import guru.bonacci.batch.model.Transaction;

/**
 * Quoting:
 * 
 * Do not use org.springframework.batch.item.validator.ValidatingItemProcessor
 * The input validation by org.springframework.validation.Validator can also be realized by using ValidatingItemProcessor provided by Spring Batch.
 * However, depending on the circumstances, it is necessary to extend it because of the following reasons, so do not use it from the viewpoint of unifying the implementation method.
 * input validation error can not be handled and processing can not be continued.
 * It is not possible to flexibly deal with data that has become an input validation error.
 * It is assumed that the processing for the data that becomes the input validation error becomes various kinds by the user (only log output, save error data to another file, etc.).
 * 
 * We'll use it anyway, because it is nice :)
 */
@Component
public class ReferenceValidatingItemProcessor extends ValidatingItemProcessor<Transaction> {


	public ReferenceValidatingItemProcessor(ReferenceValidator validator) {
		super(validator);
	}
}
