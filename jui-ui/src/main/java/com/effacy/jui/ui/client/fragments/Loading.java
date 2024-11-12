package com.effacy.jui.ui.client.fragments;

import com.effacy.jui.core.client.dom.builder.ContainerBuilder;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.ElementBuilder;
import com.effacy.jui.core.client.dom.builder.IDomInsertableContainer;

/**
 * Displays a loading indicator (which is a block with slightly rounded corners
 * and that "pulses").
 * <p>
 * For reference the background effect is implemented with the CSS
 * <code>animation: jui-animation-waiting 1s ease-in infinite;</code>.
 */
public class Loading {

    public static LoadingFragment $() {
        return new LoadingFragment ();
    }

    public static LoadingFragment $(IDomInsertableContainer<?> parent) {
        LoadingFragment frg = $ ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    public static class LoadingFragment extends BaseFragment<LoadingFragment> {

        /**
         * See {@link #dark()}.
         */
        private boolean dark;

        /**
         * Darken the loading indicator (generally for use when there is a background).
         * <p>
         * Alternatively set a background via {@link #css(String)}.
         * 
         * @return this fragment.
         */
        public LoadingFragment dark() {
            this.dark = true;
            return this;
        }

        @Override
        protected ElementBuilder createRoot(ContainerBuilder<?> parent) {
            return Div.$ (parent).style ("juiLoading").$ (item -> {
                if (dark)
                    item.style("juiLoading_dark");
            });
        }
    }

}
