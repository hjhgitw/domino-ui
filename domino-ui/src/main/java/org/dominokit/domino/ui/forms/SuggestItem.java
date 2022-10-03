///*
// * Copyright © 2019 Dominokit
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package org.dominokit.domino.ui.forms;
//
//import org.dominokit.domino.ui.dropdown.DropdownAction;
//import org.dominokit.domino.ui.icons.BaseIcon;
//import org.dominokit.domino.ui.icons.Icons;
//import org.dominokit.domino.ui.menu.AbstractMenuItem;
//import org.dominokit.domino.ui.style.Color;
//
///**
// * A component that represent a suggestion in the {@link SuggestBox}
// *
// * @param <V> the type of the SuggestItem value
// */
//public class SuggestItem<V> extends AbstractMenuItem<V, SuggestItem<V>> {
//
//  /**
//   * @param value V
//   * @param displayValue String
//   */
//  public SuggestItem(V value, String displayValue) {
//    this(value, displayValue, Icons.ALL.text_mdi());
//  }
//
//  /**
//   * @param value V
//   * @param displayValue String
//   * @param icon {@link org.dominokit.domino.ui.icons.MdiIcon}
//   */
//  public SuggestItem(V value, String displayValue, BaseIcon<?> icon) {
//    element = DropdownAction.create(value, displayValue, icon);
//    this.value = value;
//    this.displayValue = displayValue;
//  }
//
//  /**
//   * Creates a SuggestItem with a String value, the same String will be used for displayValue
//   *
//   * @param value String
//   * @return new SuggestItem instance
//   */
//  public static SuggestItem<String> create(String value) {
//    return new SuggestItem<>(value, value);
//  }
//
//  /**
//   * @param value V
//   * @param displayValue String
//   * @param <V> the type of the SuggestItem value
//   * @return new SuggestItem instance
//   */
//  public static <V> SuggestItem<V> create(V value, String displayValue) {
//    return new SuggestItem<>(value, displayValue);
//  }
//
//  /**
//   * @param value V
//   * @param displayValue String
//   * @param icon {@link BaseIcon<?>}
//   * @param <V> the type of the SuggestItem value
//   * @return new SuggestItem instance
//   */
//  public static <V> SuggestItem<V> create(V value, String displayValue, BaseIcon<?> icon) {
//    return new SuggestItem<>(value, displayValue, icon);
//  }
//
//  /**
//   * highlight part of the SuggestItem display string that matches the specified value with the
//   * specified color
//   *
//   * @param value String
//   * @param highlightColor {@link Color}
//   */
//  public void highlight(String value, Color highlightColor) {
//    element.highlight(value, highlightColor);
//  }
//
//  /** @return the SuggestItem as {@link DropdownAction} */
//  public DropdownAction<V> asDropDownAction() {
//    return element;
//  }
//
//  /** @return the V value of this SuggestItem */
//  public V getValue() {
//    return element.getValue();
//  }
//
//  /** @return String */
//  public String getDisplayValue() {
//    return displayValue;
//  }
//}
