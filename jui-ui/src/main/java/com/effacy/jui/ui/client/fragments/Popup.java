package com.effacy.jui.ui.client.fragments;

import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.platform.util.client.Carrier;

import elemental2.dom.Element;

/**
 * Fragment for a simple inline popup display.
 * <p>
 * The popup is initially hidden by default. It can be shown/hidden by obtaining
 * a {@link PopupHandler} instance by calling {@link PopupFragment#handler()} on
 * the created fragment.
 */
public class Popup {

    /**
     * The class for hiding the popup.
     */
    public static final String HIDDEN = "juiPopup-hidden";

    public static PopupFragment $() {
        return new PopupFragment ();
    }

    public static PopupFragment $(IDomInsertableContainer<?> parent) {
        PopupFragment frg = $ ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    /**
     * Used to control a specific instance of the popup. This is obtained by calling
     * {@link PopupFragment#handler()}.
     */
    public interface PopupHandler {
    
        /**
         * Shows the popup.
         */
        public void show();

        /**
         * Hides the popup.
         */
        public void hide();
    }

    /************************************************************************
     * Implementation
     ************************************************************************/

    public static class PopupFragment extends BaseFragmentWithChildren<PopupFragment> {

        /**
         * See {@link #width(Length)}.
         */
        private Length width;

        /**
         * See {@link #hidden(boolean)}.
         */
        private boolean hidden = true;

        /**
         * Sets the (maximum) width of the popup (but not more than 80% of the screen).
         * 
         * @param width
         *              the width.
         * @return this fragment.
         */
        public PopupFragment width(Length width) {
            this.width = width;
            return this;
        }

        /**
         * Sets whether the popup is initially hidden.
         * <p>
         * By default the popup is hidden.
         * 
         * @param hidden
         *               {@code true} if hidden.
         * @return this fragment.
         */
        public PopupFragment hidden(boolean hidden) {
            this.hidden = hidden;
            return this;
        }

        /**
         * Creates a handler to control the popup. This applies to a specific instance
         * of the popup.
         * 
         * @return the handler.
         */
        public PopupHandler handler() {
            Carrier<Element> popupEl = Carrier.of();
            use(n -> popupEl.set((Element) n));
            return new PopupHandler() {

                @Override
                public void show() {
                    popupEl.get().classList.remove(HIDDEN);
                }

                @Override
                public void hide() {
                    popupEl.get().classList.add(HIDDEN);
                }
            };
        }

        @Override
        public void build(ContainerBuilder<?> parent) {
            Div.$ (parent).style("juiPopup").$ (popup -> {
                if (hidden)
                    popup.style("juiPopup-hidden");
                if (width != null)
                    popup.css("--juiPopup-width-max: " + width + ";");
                Div.$(popup).style("juiPopup-mask");
                Carrier<Element> rootEl = Carrier.of();
                popup.use(n -> rootEl.set((Element) n));
                adorn (popup);
                Div.$(popup).style("juiPopup-inner").$(
                    Div.$().style("juiPopup-upper").$(upper -> {
                        Btn.$(upper, "close")
                            .variant(Btn.Variant.TEXT_COMPACT)
                            .onclick(() -> rootEl.get().classList.add(HIDDEN));
                    }),
                    Div.$().style("juiPopup-body").$ (body -> {
                        children.forEach (child -> {
                            body.insert (child);
                        });
                    })
                );
            });
        }
    }

}
