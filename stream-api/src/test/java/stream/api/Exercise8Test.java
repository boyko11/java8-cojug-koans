package stream.api;

import common.test.tool.annotation.Necessity;
import common.test.tool.dataset.ClassicOnlineStore;
import common.test.tool.entity.Customer;
import common.test.tool.entity.Item;
import common.test.tool.entity.Shop;

import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class Exercise8Test extends ClassicOnlineStore {

    @Test
    @Necessity(false)
    public void itemsNotOnSale() {
        Stream<Customer> customerStream = this.mall.getCustomerList().stream();
        Stream<Shop> shopStream = this.mall.getShopList().stream();

        /**
         * Create a set of item names that are in {@link Customer.wantToBuy} but not on sale in any shop.
         */
        Set<String> itemSetOnSale = shopStream.flatMap((shop) -> shop.getItemList().stream()).map(Item::getName).collect(Collectors.toSet());
        Set<String> itemSetNotOnSale = customerStream.flatMap((customer) -> customer.getWantToBuy().stream()).map(Item::getName).collect(Collectors.toSet());
        itemSetNotOnSale.removeAll(itemSetOnSale);

        assertThat(itemSetNotOnSale, hasSize(3));
        assertThat(itemSetNotOnSale, hasItems("bag", "pants", "coat"));
    }

    @Test
    @Necessity(false)
    public void havingEnoughMoney() {
    	
        Stream<Customer> customerStream = this.mall.getCustomerList().stream();
        Stream<Shop> shopStream = this.mall.getShopList().stream();

        /**
         * Create a customer's name list including who are having enough money to buy all items they want which is on sale.
         * Items that are not on sale can be counted as 0 money cost.
         * If there is several same items with different prices, customer can choose the cheapest one.
         */
        List<Item> onSale = shopStream.flatMap((shop) -> shop.getItemList().stream()).collect(Collectors.toList());
        Predicate<Customer> havingEnoughMoney = (customer) -> {
        	
        	int totalWantedCost = 0;
        	for(Item itemWantToBuy : customer.getWantToBuy()) {
        		List<Item> itemsOnSale = onSale.stream().filter((item) -> item.getName().equalsIgnoreCase(itemWantToBuy.getName())).collect(Collectors.toList());
        		if(itemsOnSale != null && itemsOnSale.isEmpty() == false) {
        			totalWantedCost += itemsOnSale.stream().min(Comparator.comparing(Item::getPrice)).get().getPrice();
        		}
        	}
        	return totalWantedCost <= customer.getBudget();
        };
        List<String> customerNameList = customerStream.filter(havingEnoughMoney).map(Customer::getName).collect(Collectors.toList());

        assertThat(customerNameList, hasSize(7));
        assertThat(customerNameList, hasItems("Joe", "Patrick", "Chris", "Kathy", "Alice", "Andrew", "Amy"));
    }
}
