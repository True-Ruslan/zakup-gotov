package io.github.trueruslan.zakupgotov.recipe;

import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;

record RecipeShoppingMergeKey(ShoppingRequirement requirement, QuantityUnit unit) {}
