package guru.bonacci.batch.validation;

import java.math.BigDecimal;

import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;
import org.springframework.stereotype.Component;

import guru.bonacci.batch.model.Transaction;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EndBalanceValidator implements Validator<Transaction> {

	
	@Override
	public void validate(Transaction tx) throws ValidationException {
		// No worries, we are being carefully precise here!
		BigDecimal start = tx.getStartBalance().setScale(2);
		BigDecimal mut = tx.getMutation().setScale(2);
		BigDecimal end = tx.getEndBalance().setScale(2);

		if (!start.add(mut).equals(end)) {
			log.debug("Houston, we have a balance problem: " + tx);
			throw new ValidationException("HEEEEEELP!");
		}	
	}
}
