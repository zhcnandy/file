import entity.DataEntity;

import java.util.Comparator;

public class DataEntityComparator implements Comparator<DataEntity> {

    @Override
    public int compare(DataEntity o1, DataEntity o2) {
        return Long.compare(o1.getTimestamp(), o2.getTimestamp());
    }
}
