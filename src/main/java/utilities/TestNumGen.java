package utilities;

import com.yahoo.ycsb.WorkloadException;
import com.yahoo.ycsb.generator.NumberGenerator;
import scenario.implementations.EShopHelper;

public class TestNumGen {
    public static void main(String[] args) throws WorkloadException {
        NumberGenerator itemCartKey =  EShopHelper.getKeyChooser("zipfian", 10, 0.99);
        for(int i =0; i <10; i++){
            System.out.println(itemCartKey.nextString());
        }

        NumberGenerator itemUpdateKey =  EShopHelper.getKeyChooser("uniform", 10, 0.99);
        for(int i =0; i <10; i++){
            System.out.println(itemUpdateKey.nextString());
        }
    }
}
