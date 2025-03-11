package org.ojalgo.optimisation;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

import java.io.IOException;
import java.io.InputStream;

class FileFormatEBMTest {

    @Test
    void testEBMParserWithEmptyLinesAndComments() {
        try (InputStream input = ExpressionsBasedModel.class.getResourceAsStream("/comment.ebm")) {
            TestUtils.assertTrue(input != null);
            ExpressionsBasedModel model = ExpressionsBasedModel.parse(input, ExpressionsBasedModel.FileFormat.EBM);
            Optimisation.Result result = model.minimise();
            TestUtils.assertTrue(result.getState().isSuccess());
            TestUtils.assertEquals(2, result.getValue(), 1e-12);
        } catch (IOException cause) {
            TestUtils.fail(cause);
        }
    }

}