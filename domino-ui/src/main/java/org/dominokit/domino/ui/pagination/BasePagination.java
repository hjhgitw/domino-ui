package org.dominokit.domino.ui.pagination;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLUListElement;
import org.dominokit.domino.ui.utils.BaseDominoElement;
import org.dominokit.domino.ui.utils.DominoElement;

import java.util.LinkedList;
import java.util.List;

import static org.jboss.elemento.Elements.*;

/**
 * Base implementation for pagination
 * @param <T> the type of the pagination
 */
public abstract class BasePagination<T extends BasePagination<T>> extends BaseDominoElement<HTMLElement, T> implements HasPagination {

    protected DominoElement<HTMLUListElement> pagesElement = DominoElement.of(ul().css("pagination"));
    protected DominoElement<HTMLElement> element = DominoElement.of(nav().add(pagesElement));
    protected DominoElement<? extends HTMLElement> activePage = DominoElement.of(li());
    protected DominoElement<? extends HTMLElement> prevElement;
    protected DominoElement<? extends HTMLElement> nextElement;

    protected List<DominoElement<? extends HTMLElement>> allPages = new LinkedList<>();
    protected PageChangedCallBack pageChangedCallBack = pageIndex -> {
    };
    protected String size = "pagination-default";

    protected int index = 1;
    protected boolean markActivePage = true;
    protected int pagesCount;
    protected int pageSize = 10;
    protected int totalCount = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public HasPagination updatePagesByTotalCount(int totalCount) {
        int pages = (totalCount / this.pageSize) + (totalCount % this.pageSize > 0 ? 1 : 0);
        this.totalCount = totalCount;
        return updatePages(pages, this.pageSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HasPagination updatePagesByTotalCount(int totalCount, int pageSize) {
        int pages = (totalCount / pageSize) + (totalCount % pageSize > 0 ? 1 : 0);
        this.totalCount = totalCount;
        return updatePages(pages, pageSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T gotoPage(int page) {
        return gotoPage(page, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T gotoPage(int page, boolean silent) {
        if (page > 0 && page <= pagesCount) {
            moveToPage(page, silent);
        }
        return (T) this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T nextPage() {
        return nextPage(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T previousPage() {
        return previousPage(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T nextPage(boolean silent) {
        moveToPage(index + 1, silent);
        return (T) this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T previousPage(boolean silent) {
        moveToPage(index - 1, silent);
        return (T) this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T gotoFirst() {
        return gotoFirst(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T gotoLast() {
        return gotoLast(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T gotoFirst(boolean silent) {
        if (this.pagesCount > 0) {
            return gotoPage(1, silent);
        }
        return (T) this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T gotoLast(boolean silent) {
        if (this.pagesCount > 0) {
            return gotoPage(pagesCount, silent);
        }
        return (T) this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T markActivePage() {
        this.markActivePage = true;
        gotoPage(activePage);
        return (T) this;
    }

    /**
     * A boolean to mark the active page automatically
     *
     * @param markActivePage true to mark the active page automatically, false otherwise
     * @return same instance
     */
    public T setMarkActivePage(boolean markActivePage) {
        this.markActivePage = markActivePage;
        if (!markActivePage) {
            activePage.style().remove("active");
        }

        return (T) this;
    }

    void gotoPage(DominoElement<? extends HTMLElement> li) {
        activePage.style().remove("active");
        activePage = li;
        if (markActivePage) {
            activePage.style().add("active");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T onPageChanged(PageChangedCallBack pageChangedCallBack) {
        this.pageChangedCallBack = pageChangedCallBack;
        return (T) this;
    }

    /**
     * Sets the size to large
     *
     * @return same instance
     */
    public T large() {
        return setSize("pagination-lg");
    }

    /**
     * Sets the size to small
     *
     * @return same instance
     */
    public T small() {
        return setSize("pagination-sm");
    }

    private T setSize(String sizeStyle) {
        pagesElement.style().remove(size);
        pagesElement.style().add(sizeStyle);
        size = sizeStyle;
        return (T) this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return (T) this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPageSize() {
        return this.pageSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int activePage() {
        return index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPagesCount() {
        return pagesCount;
    }

    protected abstract void moveToPage(int page, boolean silent);

    /**
     * {@inheritDoc}
     */
    @Override
    public HTMLElement element() {
        return element.element();
    }
}
