package guru.bonacci.batch.validation;

import java.util.HashSet;
import java.util.Set;

import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;
import org.springframework.stereotype.Component;

import guru.bonacci.batch.model.Transaction;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReferenceValidator implements Validator<Transaction> {

	
	private Set<Integer> references = new HashSet<>();
	
	@Override
	public void validate(Transaction tx) throws ValidationException {
		log.debug(tx.getReference().toString());
		
		if (!references.add(Integer.valueOf(tx.getReference()))) { //returns false when present in set
			log.debug("Houston, we have a reference problem: " + tx);
			throw new ValidationException("HEEEEEELP!");
		}	
	}
}
