package org.ojalgo.data.cluster;

import java.io.InputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.data.cluster.Point.Factory;
import org.ojalgo.netio.ASCII;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.netio.FromFileReader;
import org.ojalgo.netio.TextLineReader;

public class KaggleTest extends ClusterTests {

    static final class MallCustomer {

        static boolean filter(final String line) {
            return TextLineReader.isLineOK(line) && ASCII.isDigit(line.charAt(0));
        }

        static MallCustomer parse(final String line) {

            String[] parts = line.split(",");

            if (parts.length != 5 && !ASCII.isDigit(parts[0].charAt(0))) {
                throw new IllegalArgumentException("Invalid input line: " + line);
            }

            int customerID = Integer.parseInt(parts[0]);
            boolean gender = "Male".equals(parts[1]);
            int age = Integer.parseInt(parts[2]);
            int annualIncome = Integer.parseInt(parts[3]);
            int spendingScore = Integer.parseInt(parts[4]);

            return new MallCustomer(customerID, gender, age, annualIncome, spendingScore);
        }

        /**
         * Age
         */
        int age;
        /**
         * Annual Income (k$)
         */
        int annualIncome;
        /**
         * CustomerID
         */
        int customerID;
        /**
         * Gender (Male=true=10, Female=false=0)
         */
        boolean gender;
        /**
         * Spending Score (1-100)
         */
        int spendingScore;

        MallCustomer(final int customerID, final boolean gender, final int age, final int annualIncome, final int spendingScore) {
            super();
            this.customerID = customerID;
            this.gender = gender;
            this.age = age;
            this.annualIncome = annualIncome;
            this.spendingScore = spendingScore;
        }

    }

    static void describe(final Collection<Point> cluster) {
        BasicLogger.debug("Size: {}", cluster.size());
        BasicLogger.debug("Average: {}", Point.mean(cluster));
        BasicLogger.debug();

    }

    static void describe(final List<Map<KaggleTest.MallCustomer, float[]>> clusters) {
        Factory newFactory = Point.newFactory();
        List<Point> points = clusters.stream().flatMap(c -> c.values().stream()).map(c -> newFactory.newPoint(c)).collect(Collectors.toList());
        KaggleTest.describe(points);
    }

    static void describe(final Map<KaggleTest.MallCustomer, float[]> cluster) {
        Factory newFactory = Point.newFactory();
        List<Point> points = cluster.values().stream().map(c -> newFactory.newPoint(c)).collect(Collectors.toList());
        KaggleTest.describe(points);
    }

    /**
     * https://www.kaggle.com/datasets/vjchoudhary7/customer-segmentation-tutorial-in-python/data
     * <p>
     * https://www.kaggleusercontent.com/kf/211731631/eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0..bGWxHGVeU7-W8E4h6GAXQw.h1RcWyg0CmbXxHKtbuR6FL-cDjxmYvJMt8iFY-KATMuSqNdK9HJVII1ENb3Z5X97bVxta8DTis-mVM5-kWSUbwLzZqZjr7_YqKAYLVrEyjBcuX1WAvUSDp6m0PYGw014rhbvLBlLSSiykj3H10yx_5aBmBhSFnuLlvAx3ff5e2Wv0A2HaDXTzwibTR8fKx8KlWukMimniO951pWB2075HD4H30FKKiLaSQ0AgsA2gOj3qFiU14xG5hzlpzA_SVehLzUX4TAKuXtLCnQEpaNDdpkfY_X91JiD_zdyLzRLh7rwXoazpkFIZmBkAzemWn4STlDFDP5F-aLaLxZbb8eimZMdT7FFWOk0IYU-9bw9FxtswaQWpZTOzwsfmIhpsIqKgMZObPAIQxKsi1QN09NXiWfGRa_GNxa2sVU9XDRK0ObkGoAUaVJT9kHgJ5hIGDnI3QOLlP59BRR-AGMbbEEVey_bTGLJbSj0dd3USjdYs4oZTzXFlpIOCrZEogJm87vAErgpCRh2WKielomIJcDUIdTKeGNwLatj8l9c-LiIrHEdKncuSBXEJAPAAC34534hG4ImZ-oZxdTk1Rhyrsqhq3RNvftm9brn5okAtQbP1jm9FzLSJajGvCuNXeGCgGY_9Tpgg7W2j4xbwH9PuUZ512xa7RDNUkRBKe0fEC_HBjU.FfPHSdytJZY5KYiVkmnkag/__results___files/__results___15_0.png
     */
    @Test
    void testMallCustomerSegmentation() {

        try (InputStream input = TestUtils.getResource("kaggle", "Mall_Customers.csv");
                FromFileReader<MallCustomer> reader = new TextLineReader(input).withFilteredParser(MallCustomer::filter, MallCustomer::parse)) {

            List<MallCustomer> customers = reader.stream().collect(Collectors.toList());

            Function<MallCustomer, float[]> extractor = customer -> new float[] { customer.gender ? 10 : 0, customer.age, customer.annualIncome,
                    customer.spendingScore };

            List<Map<MallCustomer, float[]>> clusters = FeatureBasedClusterer.newAutomatic().cluster(customers, extractor);

            TestUtils.assertEquals(5, clusters.size());
            TestUtils.assertEquals(200, clusters.stream().mapToInt(Map<MallCustomer, float[]>::size).sum());

            clusters.sort(Comparator.comparing(Map<MallCustomer, float[]>::size).reversed());

            if (DEBUG) {
                BasicLogger.debug();
                BasicLogger.debug("Complete data set");
                BasicLogger.debug("============================================");
                KaggleTest.describe(clusters);
                BasicLogger.debug("Clusters (ordered by decreasing size)");
                BasicLogger.debug("============================================");
                for (Map<MallCustomer, float[]> cluster : clusters) {
                    KaggleTest.describe(cluster);
                }
            }

        } catch (Exception cause) {
            throw new RuntimeException(cause);
        }
    }

}
