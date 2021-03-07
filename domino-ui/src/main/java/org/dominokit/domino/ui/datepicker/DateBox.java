package org.dominokit.domino.ui.datepicker;

import elemental2.dom.DomGlobal;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.KeyboardEvent;
import jsinterop.base.Js;
import org.dominokit.domino.ui.forms.FormFieldsStyles;
import org.dominokit.domino.ui.forms.ValueBox;
import org.dominokit.domino.ui.forms.validations.InputAutoValidator;
import org.dominokit.domino.ui.forms.validations.ValidationResult;
import org.dominokit.domino.ui.grid.flex.FlexItem;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.icons.MdiIcon;
import org.dominokit.domino.ui.modals.ModalDialog;
import org.dominokit.domino.ui.popover.Popover;
import org.dominokit.domino.ui.popover.PopupPosition;
import org.dominokit.domino.ui.style.Styles;
import org.dominokit.domino.ui.utils.ElementUtil;
import org.gwtproject.i18n.shared.DateTimeFormat;
import org.gwtproject.i18n.shared.cldr.DateTimeFormatInfo;
import org.jboss.elemento.EventType;

import java.util.Date;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.dominokit.domino.ui.utils.ElementUtil.isEnterKey;
import static org.dominokit.domino.ui.utils.ElementUtil.isSpaceKey;
import static org.jboss.elemento.Elements.input;

/**
 * An text box for representing a {@link Date} which opens {@link DatePicker} when selecting it.
 *
 * This component provides either selecting the date using the {@link DatePicker} or typing the date based on the pattern configured to convert it to a valid {@link Date} object
 *
 * <p>For example: </p>
 * <pre>
 *     DateBox.create("Birth date").setPattern("yyyy/MM/dd")
 *
 *     DateBox.create("Birth date", new Date(), new DateTimeFormatInfoImpl_fr())
 *
 *     DateBox.create("Birth date")
 *             .setPopoverPosition(PopupPosition.TOP)
 *             .setPickerStyle(DateBox.PickerStyle.POPOVER)
 * </pre>
 *
 * @see ValueBox
 * @see DatePicker
 */
public class DateBox extends ValueBox<DateBox, HTMLInputElement, Date> {

    private final DatePicker datePicker;
    private String pattern;

    private Popover popover;
    private ModalDialog modal;
    private EventListener modalListener;
    private PopupPosition popupPosition = PopupPosition.BOTTOM;

    private PickerStyle pickerStyle;
    private Date value;
    private String invalidFormatMessage = "Invalid date format";
    private EventListener focusListener;
    private boolean openOnFocus = false;
    private boolean focused = false;
    private boolean handlerPaused = false;
    private FlexItem calendarIconContainer;
    private MdiIcon calendarIcon;
    private boolean openOnClick = true;
    private boolean parseStrict;
    private Date valueOnOpen;

    public DateBox() {
        this(new Date());
    }

    public DateBox(String label) {
        this(label, new Date());
    }

    public DateBox(Date date) {
        this("", date);
    }

    public DateBox(String label, Date date) {
        super("text", label);
        this.datePicker = DatePicker.create(date);
        initDateBox();
    }

    public DateBox(String label, Date date, Date minDate, Date maxDate) {
        super("text", label);
        this.datePicker = DatePicker.create(date, minDate, maxDate);
        initDateBox();
    }

    public DateBox(String label, Date date, DateTimeFormatInfo dateTimeFormatInfo) {
        super("text", label);
        this.datePicker = DatePicker.create(date, dateTimeFormatInfo);
        initDateBox();
    }

    public DateBox(String label, Date date, DateTimeFormatInfo dateTimeFormatInfo, Date minDate, Date maxDate) {
        super("text", label);
        this.datePicker = DatePicker.create(date, dateTimeFormatInfo, minDate, maxDate);
        initDateBox();
    }

    private void initDateBox() {
        this.pattern = this.datePicker.getDateTimeFormatInfo().dateFormatFull();
        this.datePicker.addDateSelectionHandler((date, dateTimeFormatInfo) -> {
            if (!handlerPaused) {
                value(date);
            }
        });

        this.valueOnOpen = value;

        getInputElement().addEventListener(EventType.focus.getName(), evt -> focused = true);
        getInputElement().addEventListener("focusin", evt -> focused = true);
        getInputElement().addEventListener(EventType.blur.getName(), evt -> focused = false);
        getInputElement().addEventListener("focusout", evt -> focused = false);

        this.modalListener = evt -> {
            if (!openOnFocus || focused) {
                modal.open();
                this.valueOnOpen = getValue();
            }
        };
        ElementUtil.onDetach(element(), mutationRecord -> removeBox());

        datePicker.addCloseHandler(this::close);
        datePicker.addResetHandler(() -> setValue(valueOnOpen));

        datePicker.addClearHandler(() -> value(null));
        setPickerStyle(PickerStyle.MODAL);

        datePicker.setBackgroundHandler((oldBackground, newBackground) -> {
            if (nonNull(modal)) {
                modal.getHeaderContainerElement().style().remove(oldBackground.color().getStyle());
                modal.getHeaderContainerElement().style().add(newBackground.color().getStyle());

            }

            if (nonNull(popover)) {
                popover.getHeadingElement().style().remove(oldBackground.color().getStyle());
                popover.getHeadingElement().style().add(newBackground.color().getStyle());
            }
        });

        getInputElement().addEventListener(EventType.keypress.getName(), evt -> {
            KeyboardEvent keyboardEvent = Js.cast(evt);
            if (isEnterKey(keyboardEvent) || isSpaceKey(keyboardEvent)) {
                open();
            }
        });
        addValidator(() -> {
            try {
                if (isEmpty()) {
                    return ValidationResult.valid();
                }
                getFormattedValue(getInputElement().element().value);
                return ValidationResult.valid();
            } catch (IllegalArgumentException e) {
                return ValidationResult.invalid(invalidFormatMessage);
            }
        });
        getInputElement().addEventListener("change", evt -> {
            String value = getInputElement().element().value;
            if (value.isEmpty()) {
                clear();
            } else {
                try {
                    value(getFormattedValue(value));
                } catch (IllegalArgumentException ignored) {
                    DomGlobal.console.warn("Unable to parse date value " + value);
                }
            }
        });
    }

    private void removeBox() {
        if (nonNull(popover)) {
            popover.close();
            popover.removeAttachObserver();
            popover.removeDetachObserver();
        }
        if (nonNull(modal)) {
            modal.element().remove();
        }
    }

    public void close() {
        if (nonNull(popover))
            popover.close();
        if (nonNull(modal) && modal.isOpen())
            modal.close();
    }

    private Date getFormattedValue(String value) throws IllegalArgumentException {
        DateTimeFormatInfo dateTimeFormatInfo = datePicker.getDateTimeFormatInfo();
        if (parseStrict) {
            return Formatter.getFormat(this.pattern, dateTimeFormatInfo).parseStrict(value);
        }
        return Formatter.getFormat(this.pattern, dateTimeFormatInfo).parse(value);
    }

    /**
     * Creates date box with no label
     *
     * @return new instance
     */
    public static DateBox create() {
        return new DateBox();
    }

    /**
     * Creates date box with a {@code label}
     *
     * @param label the field label
     * @return new instance
     */
    public static DateBox create(String label) {
        return new DateBox(label);
    }

    /**
     * Creates date box with a {@link Date}
     *
     * @param date the field value
     * @return new instance
     */
    public static DateBox create(Date date) {
        return new DateBox(date);
    }

    /**
     * Creates date box with label and {@link Date}
     *
     * @param label the field label
     * @param date the field value
     * @return new instance
     */
    public static DateBox create(String label, Date date) {
        return new DateBox(label, date);
    }

    /**
     * Creates date box with label, {@link Date} and a date time format
     *
     * @param label the field label
     * @param date the field value
     * @param dateTimeFormatInfo the {@link DateTimeFormatInfo}
     * @return new instance
     */
    public static DateBox create(String label, Date date, DateTimeFormatInfo dateTimeFormatInfo) {
        return new DateBox(label, date, dateTimeFormatInfo);
    }

    /**
     * Parse strict means that if the value is not valid, then an exception will be thrown, otherwise the value will wrapped around as needed
     *
     * @param parseStrict true to enable parse strict
     * @return same instance
     * @see DateTimeFormat#parseStrict(String)
     * @see DateTimeFormat#parse(String)
     */
    public DateBox setParseStrict(boolean parseStrict) {
        this.parseStrict = parseStrict;
        return this;
    }

    /**
     * Sets the {@link Pattern} of the field
     *
     * @param pattern the new {@link Pattern}
     * @return same instance
     */
    public DateBox setPattern(Pattern pattern) {
        switch (pattern) {
            case FULL:
                this.pattern = datePicker.getDateTimeFormatInfo().dateFormatFull();
                return this;
            case LONG:
                this.pattern = datePicker.getDateTimeFormatInfo().dateFormatLong();
                return this;
            case MEDIUM:
                this.pattern = datePicker.getDateTimeFormatInfo().dateFormatMedium();
                return this;
            case SHORT:
                this.pattern = datePicker.getDateTimeFormatInfo().dateFormatShort();
                return this;
            default:
                return this;
        }
    }

    /**
     * Sets a custom pattern.
     * <p>
     * More information of defining the pattern can be found under <a href="http://www.gwtproject.org/javadoc/latest/com/google/gwt/i18n/client/DateTimeFormat.html">GWT DateTimeFormat</a>
     *
     * @param pattern the new pattern
     * @return same instance
     */
    public DateBox setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return isNull(value) && getInputElement().element().value.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void clearValue() {
        value(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSetValue(Date value) {
        if (nonNull(value)) {
            handlerPaused = true;
            this.datePicker.setDate(value);
            this.handlerPaused = false;
            setStringValue(this.datePicker.getDate(), datePicker.getDateTimeFormatInfo());
            this.value = this.datePicker.getDate();
        } else {
            setStringValue(null, datePicker.getDateTimeFormatInfo());
            this.value = null;
        }
    }

    private void setStringValue(Date date, DateTimeFormatInfo dateTimeFormatInfo) {
        if (nonNull(date))
            this.getInputElement().element().value = getFormatted(date, dateTimeFormatInfo);
        else
            this.getInputElement().element().value = "";
        this.value = date;
    }

    private String getFormatted(Date date, DateTimeFormatInfo dateTimeFormatInfo) {
        return Formatter.getFormat(this.pattern, dateTimeFormatInfo).format(date);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Date getValue() {
        return this.value;
    }

    @Override
    protected HTMLInputElement createInputElement(String type) {
        return input("text")
                .element();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DateBox setPlaceholder(String placeholder) {
        super.setPlaceholder(placeholder);
        if (nonNull(modal)) {
            modal.setTitle(placeholder);
            modal.getHeaderContainerElement().style().add(datePicker.getColorScheme().color().getStyle());
        }

        if (nonNull(popover)) {
            popover.getHeaderText().textContent = placeholder;
            popover.getHeadingElement().style().add(datePicker.getColorScheme().color().getStyle());
        }
        return this;
    }

    /**
     * Sets the {@link PickerStyle}
     *
     * @param pickerStyle the new {@link PickerStyle}
     * @return same instance
     */
    public DateBox setPickerStyle(PickerStyle pickerStyle) {
        if (PickerStyle.MODAL.equals(pickerStyle)) {
            showInModal();
        } else {
            showInPopOver();
        }
        return this;
    }

    private void showInPopOver() {
        if (!PickerStyle.POPOVER.equals(this.pickerStyle)) {
            if (nonNull(modal)) {
                element().removeEventListener(EventType.click.getName(), modalListener);
                if (modal.isOpen()) {
                    modal.close();
                }
                modal.element().remove();
            }

            if (isNull(popover)) {
                popover = Popover.createPicker(this, this.datePicker)
                        .position(this.popupPosition)
                        .styler(style -> style.add(DatePickerStyles.PICKER_POPOVER));

                popover.addOpenListener(() -> this.valueOnOpen = getValue());

                popover.getHeadingElement()
                        .style()
                        .add(Styles.align_center, datePicker.getColorScheme().color().getStyle());
            }
        }

        this.pickerStyle = PickerStyle.POPOVER;
    }

    private void showInModal() {
        if (!PickerStyle.MODAL.equals(this.pickerStyle)) {
            if (nonNull(popover)) {
                this.popover.discard();
            }

            if (isNull(modal)) {
                this.modal = ModalDialog.createPickerModal(getPlaceholder(), this.datePicker.element());
                element().addEventListener(EventType.click.getName(), modalListener);
            }
        }
        this.pickerStyle = PickerStyle.MODAL;
    }

    /**
     * @return The {@link DatePicker}
     */
    public DatePicker getDatePicker() {
        return datePicker;
    }

    /**
     * Sets the position of the model if the {@link PickerStyle} is {@link PickerStyle#POPOVER}
     *
     * @param popoverPosition the new {@link PopupPosition}
     * @return same instance
     */
    public DateBox setPopoverPosition(PopupPosition popoverPosition) {
        this.popupPosition = popoverPosition;
        if (nonNull(this.popover))
            this.popover.position(this.popupPosition);
        return this;
    }

    /**
     * Opens the date picker when the element is focused
     *
     * @return same instance
     */
    public DateBox openOnFocus() {
        this.openOnFocus = true;
        if (nonNull(getFocusEventListener())) {
            getInputElement().removeEventListener(EventType.focus.getName(), getFocusEventListener());
        }
        focusListener = evt -> {
            getInputElement().removeEventListener(EventType.focus.getName(), getFocusEventListener());
            open();
        };
        getInputElement().addEventListener(EventType.focus.getName(), getFocusEventListener());
        modal.addCloseListener(() -> {
            getInputElement().element().focus();
            getInputElement().addEventListener(EventType.focus.getName(), getFocusEventListener());
        });

        modal.addOpenListener(() -> getInputElement().removeEventListener(EventType.focus.getName(), getFocusEventListener()));
        return this;
    }

    private EventListener getFocusEventListener() {
        return focusListener;
    }

    /**
     * Opens the date picker based on the {@link PickerStyle} defined
     */
    public void open() {
        if (PickerStyle.MODAL.equals(this.pickerStyle)) {
            modal.open();
        } else {
            popover.show();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DateBox disable() {
        disableModal();
        disablePopover();
        return super.disable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DateBox setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        if (readOnly) {
            getInputElement().style().add(FormFieldsStyles.READONLY);
            disableModal();
            disablePopover();
        } else if (isEnabled()) {
            enableModal();
            enablePopover();
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DateBox enable() {
        enableModal();
        enablePopover();
        return super.enable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStringValue() {
        if (nonNull(value)) {
            return Formatter.getFormat(this.pattern, datePicker.getDateTimeFormatInfo()).format(value);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FlexItem createMandatoryAddOn() {
        calendarIcon = Icons.ALL.calendar_mdi();
        calendarIcon.clickable()
                .addClickListener(evt -> {
                    evt.stopPropagation();
                    if (!isDisabled()) {
                        open();
                    }
                });
        calendarIconContainer = FlexItem.create();
        return calendarIconContainer
                .appendChild(calendarIcon);
    }

    /**
     * @return The calendar icon container element
     */
    public FlexItem getCalendarIconContainer() {
        return calendarIconContainer;
    }

    /**
     * @return The calendar icon element
     */
    public MdiIcon getCalendarIcon() {
        return calendarIcon;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AutoValidator createAutoValidator(AutoValidate autoValidate) {
        return new InputAutoValidator<>(getInputElement(), autoValidate);
    }

    /**
     * Sets the error message when the date is not well formatted
     *
     * @param invalidFormatMessage the new message
     * @return same instance
     */
    public DateBox setInvalidFormatMessage(String invalidFormatMessage) {
        this.invalidFormatMessage = invalidFormatMessage;
        return this;
    }

    /**
     * @return The {@link ModalDialog} of the date picker
     */
    public Optional<ModalDialog> getModal() {
        return Optional.of(this.modal);
    }

    /**
     * @return The {@link Popover} of the date picker
     */
    public Optional<Popover> getPopover() {
        return Optional.of(this.popover);
    }

    /**
     * @return true if the date picker should be opened when clicking on the field, false otherwise
     */
    public boolean isOpenOnClick() {
        return openOnClick;
    }

    /**
     * Sets if the date picker should be opened when clicking on the field
     *
     * @param openOnClick true to open the date picker when clicking on the field
     * @return same instance
     */
    public DateBox setOpenOnClick(boolean openOnClick) {
        this.openOnClick = openOnClick;
        element().removeEventListener(EventType.click.getName(), modalListener);
        if(openOnClick){
            element().addEventListener(EventType.click.getName(), modalListener);
        }
        return this;
    }

    private void disablePopover() {
        if (nonNull(popover)) {
            popover.disable();
        }
    }

    private void disableModal() {
        if (nonNull(modal)) {
            modal.disable();
        }
    }

    private void enablePopover() {
        if (nonNull(popover)) {
            popover.enable();
        }
    }

    private void enableModal() {
        if (nonNull(modal)) {
            modal.enable();
        }
    }

    private static class Formatter extends DateTimeFormat {

        protected Formatter(String pattern) {
            super(pattern);
        }

        protected Formatter(String pattern, DateTimeFormatInfo dtfi) {
            super(pattern, dtfi);
        }

        public static DateTimeFormat getFormat(String pattern, DateTimeFormatInfo dateTimeFormatInfo) {
            return DateTimeFormat.getFormat(pattern, dateTimeFormatInfo);
        }
    }

    /**
     * A predefined patterns for the date
     *
     * see <a href="http://cldr.unicode.org/translation/date-time-1/date-time-patterns">CLDR date time patterns</a> for more details
     */
    public enum Pattern {
        /**
         * A full date format
         */
        FULL,
        /**
         * A long date format
         */
        LONG,
        /**
         * A medium date format
         */
        MEDIUM,
        /**
         * A short date format
         */
        SHORT
    }

    /**
     * The style of the date picker
     */
    public enum PickerStyle {
        /**
         * Shows the date picker in a model
         */
        MODAL,
        /**
         * Shows the date picker in a popover
         */
        POPOVER
    }

}
