package org.ojalgo.netio;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.ojalgo.access.Structure1D.IndexMapper;
import org.ojalgo.array.LongToNumberMap;
import org.ojalgo.array.LongToNumberMap.MapFactory;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.type.context.NumberContext;

public final class TableData<R> {

    private static final NumberContext GENERAL = NumberContext.getGeneral(8);

    private static final MapFactory<Double> COLUMN_FACTORY = LongToNumberMap.factory(Primitive64Array.FACTORY);

    private final Map<String, LongToNumberMap<Double>> myColumns = new HashMap<>();
    private final IndexMapper<R> myRowIndexMapper;
    private final Set<R> myRows = new TreeSet<>();

    public TableData(final IndexMapper<R> rowIndexMapper) {
        super();
        myRowIndexMapper = rowIndexMapper;
    }

    public double doubleValue(final R row, final String col) {
        return myColumns.computeIfAbsent(col, (c) -> COLUMN_FACTORY.make()).doubleValue(myRowIndexMapper.toIndex(row));
    }

    public CharSequence print() {

        final StringBuilder builder = new StringBuilder();

        final Set<String> columnKeys = myColumns.keySet();

        builder.append("Dimension");
        for (final String col : columnKeys) {
            builder.append(";");
            builder.append(col);
        }

        builder.append(LineTerminator.UNIX);

        for (final R row : myRows) {
            builder.append(row);
            for (final String col : columnKeys) {
                builder.append(";");
                final double value = myColumns.get(col).doubleValue(myRowIndexMapper.toIndex(row));
                if (!Double.isNaN(value)) {
                    builder.append(GENERAL.format(value));
                }
            }
            builder.append(LineTerminator.UNIX);
        }

        return builder;
    }

    public void put(final R row, final String col, final double value) {
        myRows.add(row);
        myColumns.computeIfAbsent(col, (c) -> COLUMN_FACTORY.make()).put(myRowIndexMapper.toIndex(row), value);
    }

}
