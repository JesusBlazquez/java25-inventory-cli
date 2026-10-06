package io.github.jesusblazquez.inventory.storage;

import io.github.jesusblazquez.inventory.domain.Inventory;

/** Where the inventory lives between runs. */
public interface InventoryStore {

    Inventory load();

    void save(Inventory inventory);
}
