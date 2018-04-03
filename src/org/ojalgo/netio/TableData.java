package org.ojalgo.netio;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.ojalgo.access.Structure1D.IndexMapper;
import org.ojalgo.array.LongToNumberMap;
import org.ojalgo.array.LongToNumberMap.MapFactory;
import org.ojalgo.array.Primitive64Array;

public final class TableData<R> {

    private static final MapFactory<Double> COLUMN_FACTORY = LongToNumberMap.factory(Primitive64Array.FACTORY);

    private Map<String, LongToNumberMap<Double>> myColumns = new HashMap<>();
    private Set<R> myRows = new TreeSet<>();
    private IndexMapper<R> myRowIndexMapper;

    public TableData(IndexMapper<R> rowIndexMapper) {
        super();
        myRowIndexMapper = rowIndexMapper;
    }

    public double doubleValue(R row, String col) {
        return myColumns.computeIfAbsent(col, (c) -> COLUMN_FACTORY.make()).doubleValue(myRowIndexMapper.toIndex(row));
    }

    public CharSequence print() {

        StringBuilder builder = new StringBuilder();

        Set<String> columnKeys = myColumns.keySet();

        builder.append("Dimension");
        for (String col : columnKeys) {
            builder.append(";");
            builder.append(col);
        }

        for (R row : myRows) {
            builder.append(row);
            for (String col : columnKeys) {
                builder.append(";");
                builder.append(myColumns.get(col).doubleValue(myRowIndexMapper.toIndex(row)));
            }
        }

        return builder;
    }

    public void put(R row, String col, double value) {
        myRows.add(row);
        myColumns.computeIfAbsent(col, (c) -> COLUMN_FACTORY.make()).put(myRowIndexMapper.toIndex(row), value);
    }

}
