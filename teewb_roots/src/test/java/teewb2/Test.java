package teewb2;

import java.util.ArrayList;
import java.util.List;

import com.transformedge.teewb.processor.ProcessExceptionCodes;
import com.transformedge.teewb.request.CsvFailedValidationOutput;
import com.transformedge.teewb.request.Invoice;

public class Test {
	public static void main(String[] args) {
		Invoice invoice = new Invoice();
        List<CsvFailedValidationOutput> failedValidationOutputs = new ArrayList<>();

		invoice.addField("id", "data");
		CsvFailedValidationOutput csvFailedValidationOutput = new CsvFailedValidationOutput(invoice,
				"Field: " + "column name" + " should be a number but has invalid value: " + "data",
				ProcessExceptionCodes.VALIDATION_ERROR);

        failedValidationOutputs.add(csvFailedValidationOutput);
        failedValidationOutputs.add(csvFailedValidationOutput);

		System.out.println(failedValidationOutputs);
	}
}
