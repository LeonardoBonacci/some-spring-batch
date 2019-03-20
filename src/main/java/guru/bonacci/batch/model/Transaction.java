package guru.bonacci.batch.model;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "record")
@XmlAccessorType(FIELD)
public class Transaction {


	@XmlAttribute(name = "reference")
    private Integer reference;
    private String accountNumber, description;
    private BigDecimal startBalance, mutation, endBalance;
}
