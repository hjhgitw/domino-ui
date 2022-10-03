/*
 * Copyright © 2019 Dominokit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dominokit.domino.ui.menu;

import elemental2.dom.HTMLUListElement;
import org.dominokit.domino.ui.utils.DominoElement;
import org.dominokit.domino.ui.utils.LazyChild;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.dominokit.domino.ui.menu.MenuStyles.*;

public class MenuItemsGroup<V, I extends AbstractMenuItem<V, I>>
    extends AbstractMenuItem<V, MenuItemsGroup<V, I>> {

  private final Menu<V> menu;
  private List<I> menuItems = new ArrayList<>();

  private LazyChild<DominoElement<HTMLUListElement>> itemsListElement;

  public MenuItemsGroup(Menu<V> menu) {
    this.menu = menu;
    removeCss(MENU_ITEM);
    addCss(MENU_GROUP);
    linkElement.removeCss(MENU_ITEM_ANCHOR);
    linkElement.addCss(MENU_GROUP_HEADER);
    itemsListElement = LazyChild.of(DominoElement.ul().addCss(MENU_ITEMS_LIST), root);
  }

  public MenuItemsGroup<V,I> appendChild(I menuItem) {
    if (nonNull(menuItem)) {
      menuItem.bindToGroup(this);
      itemsListElement.get().appendChild(menuItem);
      menuItems.add(menuItem);
      menuItem.setParent(menu);
      menu.onItemAdded(menuItem);
    }
    return this;
  }

  public MenuItemsGroup<V,I> removeItem(I menuItem) {
    if (this.menuItems.contains(menuItem)) {
      menuItem.unbindGroup();
      menuItem.remove();
      this.menuItems.remove(menuItem);
    }
    return this;
  }

  public List<I> getMenuItems() {
    return menuItems;
  }

  @Override
  public boolean onSearch(String token, boolean caseSensitive) {
    return menuItems.stream().anyMatch(menuItem -> menuItem.onSearch(token, caseSensitive));
  }
}
