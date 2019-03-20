package guru.bonacci.batch.listen;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.springframework.batch.core.SkipListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import guru.bonacci.batch.model.Transaction;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FileAppendingSkipListener implements SkipListener<Transaction, Transaction> {
	
	@Value("${file.path.errors:errors.txt}")
	private String fileName;
	
	
    @Override
    public void onSkipInRead(Throwable t) {
        log.error("Ooh nooo!!");
    }
 
    @Override
    public void onSkipInWrite(Transaction tx, Throwable t) {
        log.error("Ooh nooo!! {}", tx);
    }
 
    //TODO improve error report writing mechanism
    @Override
    public void onSkipInProcess(Transaction tx, Throwable t) {
        log.debug("Fraud or human failure: {}", tx);

        // WARNING: BEGIN HACK
		try (PrintStream stream = new PrintStream(new FileOutputStream(fileName, true), true, "UTF-8")) {
			stream.write((tx.getReference() + " - " + tx.getDescription() + "\r\n").getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
        // WARNING: END HACK
    }
}