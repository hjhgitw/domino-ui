package org.dominokit.domino.ui.loaders;

import elemental2.dom.HTMLDivElement;
import org.dominokit.domino.ui.style.Style;
import org.dominokit.domino.ui.style.Styles;
import org.dominokit.domino.ui.utils.DominoElement;
import org.jboss.elemento.IsElement;

import static org.dominokit.domino.ui.loaders.LoaderStyles.*;
import static org.jboss.elemento.Elements.div;

/**
 * Bounce pulse loader implementation
 */
public class BouncePulseLoader extends BaseLoader<BouncePulseLoader> implements IsElement<HTMLDivElement> {

    private final HTMLDivElement progress1 = div().css(WAIT_ME_PROGRESS_ELEM_1).style("background-color:#000").element();
    private final HTMLDivElement progress2 = div().css(WAIT_ME_PROGRESS_ELEM_2).style("background-color:#000").element();
    private final HTMLDivElement progress3 = div().css(WAIT_ME_PROGRESS_ELEM_3).style("background-color:#000").element();

    private final HTMLDivElement loader = div()
            .css(WAIT_ME_PROGRESS)
            .css(BOUNCE_PULSE)
            .add(progress1)
            .add(progress2)
            .add(progress3)
            .element();

    private final HTMLDivElement content = div()
            .css(WAIT_ME_CONTENT)
            .css(Styles.vertical_center)
            .css(VERTICAL)
            .add(loader)
            .add(loadingText)
            .element();

    private final HTMLDivElement element = div()
            .css(WAIT_ME)
            .style("background: rgba(255, 255, 255, 0.7);")
            .add(content)
            .element();

    public BouncePulseLoader() {
        init(this);
    }

    public static BouncePulseLoader create() {
        return new BouncePulseLoader();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLoadingText(String text) {
        loadingText.textContent = text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSize(String width, String height) {
        onAttached(mutationRecord -> Style.of(loader).setWidth(width).setHeight(height));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLoadingText() {
        onAttached(mutationRecord -> loadingText.remove());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DominoElement<HTMLDivElement> getContentElement() {
        return DominoElement.of(content);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HTMLDivElement element() {
        return element;
    }
}
