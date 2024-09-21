/*******************************************************************************
 * Copyright 2024 Jeremy Buckley
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * <a href= "http://www.apache.org/licenses/LICENSE-2.0">Apache License v2</a>
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.effacy.jui.playground.ui.lessons;

import java.util.Optional;
import java.util.function.Consumer;

import com.effacy.jui.core.client.IResetable;
import com.effacy.jui.core.client.component.SimpleComponent;
import com.effacy.jui.core.client.component.StateComponent;
import com.effacy.jui.core.client.component.layout.VertLayout;
import com.effacy.jui.core.client.component.layout.VertLayout.VertLayoutData.Separator;
import com.effacy.jui.core.client.dom.INodeProvider;
import com.effacy.jui.core.client.dom.builder.Div;
import com.effacy.jui.core.client.dom.builder.P;
import com.effacy.jui.core.client.dom.builder.Wrap;
import com.effacy.jui.core.client.dom.css.CSS;
import com.effacy.jui.core.client.dom.css.Insets;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.core.client.dom.jquery.JQuery;
import com.effacy.jui.core.client.dom.jquery.JQueryElement;
import com.effacy.jui.core.client.navigation.INavigationAware;
import com.effacy.jui.core.client.navigation.INavigationHandler.NavigationContext;
import com.effacy.jui.core.client.state.LifecycleStateVariable;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.ui.client.InfoBlockCreator;
import com.effacy.jui.ui.client.button.ButtonCreator;
import com.effacy.jui.ui.client.control.TextControl;
import com.effacy.jui.ui.client.control.TextControlCreator;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.modal.Modal.Type;
import com.effacy.jui.ui.client.modal.ModalDialog.Config.ModalStyle;
import com.effacy.jui.ui.client.modal.ModalDialogCreator;
import com.effacy.jui.ui.client.modal.ModalDialogCreator.IDialogOpener;
import com.effacy.jui.ui.client.modal.ProgressSequence.Config.State;
import com.effacy.jui.ui.client.modal.ProgressSequenceCreator;
import com.effacy.jui.ui.client.panel.Panel;
import com.effacy.jui.ui.client.panel.PanelCreator;

import elemental2.dom.Element;

public class Lesson5c extends Panel implements INavigationAware, IResetable {

    public Lesson5c() {
        super (PanelCreator.config ().scrollable ().padding( Insets.em(2)).layout (VertLayout.$ ().separator (Separator.LINE).spacing (Length.em (1)).build ()));

        // This display a title and descriptive text on the page.
        add (InfoBlockCreator.<Void>$ (cfg -> {}, builder -> {
            builder.header ("Part C: Multistep dialogs", header -> {
                header.subtitle ("Here we describe how to create a multiple step dialog.");
            });
        })).update (null);

        add (ButtonCreator.build(cfg -> {
            cfg.label ("Multistep dialog 1");
            cfg.handler (() -> MultiStepDialog1.open ());
        }));

        add (ButtonCreator.build(cfg -> {
            cfg.label ("Multistep dialog 2");
            cfg.handler (() -> MultiStepDialog2.open ());
        }));

        add (ButtonCreator.build(cfg -> {
            cfg.label ("Multistep dialog 3");
            cfg.handler (() -> MultiStepDialog3.open (v -> {
                if (v.isPresent())
                    Logger.log("Outcome: " + v.get());
            }));
        }));

        add (ButtonCreator.build(cfg -> {
            cfg.label ("Multistep dialog 4");
            cfg.handler (() -> MultiStepDialog4.open (v -> {
                if (v.isPresent())
                    Logger.log("Outcome: " + v.get());
            }));
        }));

    }

    @Override
    public void onNavigateTo(NavigationContext context) {
        reset ();
    }

    public static class MultiStepDialog1 extends StateComponent<MultiStepDialog1.State> implements IResetable {

        /************************************************************************
         * Dialog support.
         ************************************************************************/

        /**
         * See {@link #open(Consumer)}.
         */
        private static IDialogOpener<Void, Void> DIALOG;

        /**
         * Opens an instance of the panel in a dialog.
         * 
         * @param cb
         *           the callback.
         */
        public static void open() {
            if (DIALOG == null)
                DIALOG = ModalDialogCreator.<Void, Void, MultiStepDialog1>dialog (new MultiStepDialog1 (), cfg -> {
                    cfg.style (ModalStyle.SEPARATED)
                            .title ("Multistep dialog 1")
                            .type (Type.CENTER)
                            .width (Length.px (500));
                }, b -> {
                    b.cancel ();
                    b.button ((c, btn) -> {
                        btn.reference ("back");
                        btn.label ("Back").icon (FontAwesome.arrowLeft ());
                        btn.left (true);
                        btn.handler (cb -> {
                            cb.contents().state().modify (v -> {
                                v.prev ();
                            });
                            if (cb.contents ().state ().isStart ())
                                cb.modal ().disable ("back");
                            cb.modal ().hide ("save");
                            cb.modal ().show ("next");
                            cb.fail ();
                        });
                    });
                    b.button ((c, btn) -> {
                        btn.reference ("next");
                        btn.label ("Next").icon (FontAwesome.arrowRight (), true);
                        btn.handler (cb -> {
                            cb.contents ().state ().modify (v -> {
                                v.next ();
                            });
                            cb.modal ().enable ("back");
                            if (cb.contents().state().isEnd()) {
                                cb.modal ().show ("save");
                                cb.modal ().hide ("next");
                            }
                            cb.fail ();
                        });
                    });
                    b.button ((c, btn) -> {
                        btn.reference ("save");
                        btn.label ("Save").icon (FontAwesome.check (), true);
                        btn.handler (cb -> {
                            cb.success ();
                        });
                    });
                }).listener (modal -> {
                    modal.disable ("back");
                    modal.show ("next");
                    modal.hide ("save");
                });
            DIALOG.open (null, null);
        }

        /************************************************************************
         * Construction.
         ************************************************************************/
        
         /**
          * Captures the state of the dialog.
          */
        public static class State extends LifecycleStateVariable<State> {

            /**
             * Enumerates the possible pages.
             */
            public enum Page {
                PAGE1, PAGE2, PAGE3;
            }

            /**
             * The current page.
             */
            private Page page = Page.PAGE1;

            /**
             * Determines if we are at the start of the dialog flow.
             * 
             * @return {@code true} if we are.
             */
            public boolean isStart() {
                return (page == Page.PAGE1);
            }

            /**
             * Determines if we are at the end of a dialog flow.
             * 
             * @return {@code true} if we are.
             */
            public boolean isEnd() {
                return (page == Page.PAGE3);
            }

            /**
             * Progress to the next step.
             */
            public void next() {
                if (page == Page.PAGE1) {
                    page = Page.PAGE2;
                } else if (page == Page.PAGE2) {
                    page = Page.PAGE3;
                }
                emit ();
            }

            /**
             * Return to the previous step.
             */
            public void prev() {
                if (page == Page.PAGE2) {
                    page = Page.PAGE1;
                } else if (page == Page.PAGE3) {
                    page = Page.PAGE2;
                }
                emit ();
            }

            public void reset() {
                page = Page.PAGE1;
                emit ();
            }

        }

        public MultiStepDialog1() {
            super (new MultiStepDialog1.State ());
        }

        @Override
        protected INodeProvider buildNode(Element el) {
            // Page 3.
            if (state ().page == State.Page.PAGE3) {
                return Wrap.$(el).$ (root -> {
                    root.css (CSS.PADDING, Insets.em (2,1));
                    P.$ (root).$ (p -> {
                        p.text ("This is the last step");
                    });
                }).build ();
            }

            // Page 2.
            if (state ().page == State.Page.PAGE2) {
                return Wrap.$(el).$ (root -> {
                    root.css (CSS.PADDING, Insets.em (2,1));
                    P.$ (root).$ (p -> {
                        p.text ("This is the second step");
                    });
                }).build ();
            }

            // Page 1.
            return Wrap.$(el).$ (root -> {
                root.css (CSS.PADDING, Insets.em (2,1));
                P.$ (root).$ (p -> {
                    p.text ("This is the first step");
                });
            }).build ();
        }

        @Override
        public void reset() {
            state ().reset ();
        }
        
    }

    public static class MultiStepDialog2 extends StateComponent<MultiStepDialog2.State> implements IResetable {

        /************************************************************************
         * Dialog support.
         ************************************************************************/

        /**
         * See {@link #open(Consumer)}.
         */
        private static IDialogOpener<Void, Void> DIALOG;

        public MultiStepDialog2() {
            super (new MultiStepDialog2.State ());
        }

        /**
         * Opens an instance of the panel in a dialog.
         * 
         * @param cb
         *           the callback.
         */
        public static void open() {
            if (DIALOG == null)
                DIALOG = ModalDialogCreator.<Void, Void, MultiStepDialog2>dialog (new MultiStepDialog2 (), cfg -> {
                    cfg.style (ModalStyle.SEPARATED)
                            .title ("Multistep dialog 2")
                            .type (Type.CENTER)
                            .width (Length.px (500));
                }, b -> {
                    b.cancel ();
                    b.button ((c, btn) -> {
                        btn.reference ("back");
                        btn.label ("Back").icon (FontAwesome.arrowLeft ());
                        btn.left (true);
                        btn.handler (cb -> {
                            if (cb.contents ().prev ().isStart ())
                                cb.modal ().disable ("back");
                            cb.modal ().hide ("save");
                            cb.modal ().show ("next");
                            cb.fail ();
                        });
                    });
                    b.button ((c, btn) -> {
                        btn.reference ("next");
                        btn.label ("Next").icon (FontAwesome.arrowRight (), true);
                        btn.handler (cb -> {
                            if (cb.contents ().next ().isEnd()) {
                                cb.modal ().show ("save");
                                cb.modal ().hide ("next");
                            }
                            cb.modal ().enable ("back");
                            cb.fail ();
                        });
                    });
                    b.button ((c, btn) -> {
                        btn.reference ("save");
                        btn.label ("Save").icon (FontAwesome.check (), true);
                        btn.handler (cb -> {
                            cb.contents ().save (outcome -> {
                                if (outcome)
                                    cb.success ();
                                else
                                    cb.fail ();
                            });
                        });
                    });
                }).listener (modal -> {
                    modal.disable ("back");
                    modal.show ("next");
                    modal.hide ("save");
                });
            DIALOG.open (null, null);
        }

        /************************************************************************
         * Construction.
         ************************************************************************/
        
         /**
          * Captures the state of the dialog.
          */
        public static class State extends LifecycleStateVariable<State> {

            /**
             * Enumerates the possible pages.
             */
            public enum Page {
                PAGE1, PAGE2, PAGE3;
            }

            /**
             * The current page.
             */
            private Page page = Page.PAGE1;

            /**
             * Determines if we are at the start of the dialog flow.
             * 
             * @return {@code true} if we are.
             */
            public boolean isStart() {
                return (page == Page.PAGE1);
            }

            /**
             * Determines if we are at the end of a dialog flow.
             * 
             * @return {@code true} if we are.
             */
            public boolean isEnd() {
                return (page == Page.PAGE3);
            }

        }

        @Override
        protected INodeProvider buildNode(Element el) {
            // Page 3.
            if (state ().page == State.Page.PAGE3) {
                return Wrap.$(el).$ (root -> {
                    root.css (CSS.PADDING, Insets.em (2,1));
                    P.$ (root).$ (p -> {
                        p.text ("This is the last step");
                    });
                }).build ();
            }

            // Page 2.
            if (state ().page == State.Page.PAGE2) {
                return Wrap.$(el).$ (root -> {
                    root.css (CSS.PADDING, Insets.em (2,1));
                    P.$ (root).$ (p -> {
                        p.text ("This is the second step");
                    });
                }).build ();
            }

            // Page 1.
            return Wrap.$(el).$ (root -> {
                root.css (CSS.PADDING, Insets.em (2,1));
                P.$ (root).$ (p -> {
                    p.text ("This is the first step");
                });
            }).build ();
        }

        public State next() {
            state().modify (v -> {
                if (v.page == State.Page.PAGE1) {
                    v.page = State.Page.PAGE2;
                } else if (v.page == State.Page.PAGE2) {
                    v.page = State.Page.PAGE3;
                }
            });
            return state ();
        }

        public State prev() {
            state().modify (v -> {
                if (v.page == State.Page.PAGE2) {
                    v.page = State.Page.PAGE1;
                } else if (v.page == State.Page.PAGE3) {
                    v.page = State.Page.PAGE2;
                }
            });
            return state ();
        }

        public void save(Consumer<Boolean> callback) {
            callback.accept (true);
        }

        @Override
        public void reset() {
            state().modify (v -> {
                v.page = State.Page.PAGE1;
            });
        }
        
    }

    public static class MultiStepDialog3 extends StateComponent<MultiStepDialog3.State> implements IResetable {

        /************************************************************************
         * Dialog support.
         ************************************************************************/

        /**
         * See {@link #open(Consumer)}.
         */
        private static IDialogOpener<Void, String> DIALOG;

        /**
         * Opens an instance of the panel in a dialog.
         * 
         * @param cb
         *           the callback.
         */
        public static void open(Consumer<Optional<String>> callback) {
            if (DIALOG == null)
                DIALOG = ModalDialogCreator.<Void, String, MultiStepDialog3>dialog (new MultiStepDialog3 (), cfg -> {
                    cfg.style (ModalStyle.SEPARATED)
                            .title ("Multistep dialog 3")
                            .type (Type.CENTER)
                            .width (Length.px (500));
                }, b -> {
                    b.cancel ();
                    b.button ((c, btn) -> {
                        btn.reference ("back");
                        btn.label ("Back").icon (FontAwesome.arrowLeft ());
                        btn.left (true);
                        btn.handler (cb -> {
                            if (cb.contents ().prev ().isStart ())
                                cb.modal ().disable ("back");
                            cb.modal ().hide ("save");
                            cb.modal ().show ("next");
                            cb.fail ();
                        });
                    });
                    b.button ((c, btn) -> {
                        btn.reference ("next");
                        btn.label ("Next").icon (FontAwesome.arrowRight (), true);
                        btn.handler (cb -> {
                            if (cb.contents ().next ().isEnd()) {
                                cb.modal ().show ("save");
                                cb.modal ().hide ("next");
                            }
                            cb.modal ().enable ("back");
                            cb.fail ();
                        });
                    });
                    b.button ((c, btn) -> {
                        btn.reference ("save");
                        btn.label ("Save").icon (FontAwesome.check (), true);
                        btn.handler (cb -> {
                            cb.contents ().process (outcome -> {
                                if (outcome.isPresent()) {
                                    c.accept (outcome);
                                    cb.success ();
                                } else
                                    cb.fail ();
                            });
                        });
                    });
                }).listener (modal -> {
                    modal.disable ("back");
                    modal.show ("next");
                    modal.hide ("save");
                });
            DIALOG.open (null, callback);
        }

        /************************************************************************
         * Construction.
         ************************************************************************/
        
         /**
          * Captures the state of the dialog.
          */
        public static class State extends LifecycleStateVariable<State> {

            /**
             * Enumerates the possible pages.
             */
            public enum Page {
                PAGE1, PAGE2, PAGE3;
            }

            /**
             * The current page.
             */
            private Page page = Page.PAGE1;

            /**
             * The first value.
             */
            private String value1;

            /**
             * The second value.
             */
            private String value2;

            /**
             * The third value.
             */
            private String value3;

            /**
             * Determines if we are at the start of the dialog flow.
             * 
             * @return {@code true} if we are.
             */
            public boolean isStart() {
                return (page == Page.PAGE1);
            }

            /**
             * Determines if we are at the end of a dialog flow.
             * 
             * @return {@code true} if we are.
             */
            public boolean isEnd() {
                return (page == Page.PAGE3);
            }

        }

        public MultiStepDialog3() {
            super (new MultiStepDialog3.State( ));
        }

        @Override
        protected INodeProvider buildNode(Element el) {
            // Page 3.
            if (state ().page == State.Page.PAGE3) {
                return Wrap.$(el).$ (root -> {
                    root.css (CSS.PADDING, Insets.em (2,1));
                    P.$ (root).$ (p -> {
                        p.text ("This is the last step");
                    });
                    root.insert (TextControlCreator.build (cfg-> {
                        cfg.placeholder ("Last field");
                        cfg.modifiedHandler ((ctl, value, prior) -> state ().value3 = value);
                    }, ctl -> ctl.setValue (state ().value3)));
                }).build ();
            }

            // Page 2.
            if (state ().page == State.Page.PAGE2) {
                return Wrap.$(el).$ (root -> {
                    root.css (CSS.PADDING, Insets.em (2,1));
                    P.$ (root).$ (p -> {
                        p.text ("This is the second step");
                    });
                    root.insert (TextControlCreator.build (cfg-> {
                        cfg.placeholder ("Second field");
                        cfg.modifiedHandler ((ctl, value, prior) -> state ().value2 = value);
                    }, ctl -> ctl.setValue (state ().value2)));
                }).build ();
            }

            // Page 1.
            return Wrap.$(el).$ (root -> {
                root.css (CSS.PADDING, Insets.em (2,1));
                P.$ (root).$ (p -> {
                    p.text ("This is the first step");
                    root.insert (TextControlCreator.build (cfg-> {
                        cfg.placeholder ("First field");
                        cfg.modifiedHandler ((ctl, value, prior) -> state ().value1 = value);
                    }, ctl -> ctl.setValue (state ().value1)));
                });
            }).build ();
        }

        public State next() {
            state ().modify (v -> {
                if (v.page == State.Page.PAGE1)
                    v.page = State.Page.PAGE2;
                else if (v.page == State.Page.PAGE2)
                    v.page = State.Page.PAGE3;
            });
            return state ();
        }

        public State prev() {
            state ().modify (v -> {
                if (v.page == State.Page.PAGE2)
                    v.page = State.Page.PAGE1;
                else if (v.page == State.Page.PAGE3)
                    v.page = State.Page.PAGE2;
            });
            return state ();
        }


        public void process(Consumer<Optional<String>> callback) {
            callback.accept (Optional.of (state ().value1 + " " + state ().value2 + " " + state ().value3));
        }

        @Override
        public void reset() {
            state ().modify (v -> {
                v.page = State.Page.PAGE1;
            });
        }
        
    }

    public static class MultiStepDialog4 extends SimpleComponent implements IResetable {

        /************************************************************************
         * Dialog support.
         ************************************************************************/

        /**
         * See {@link #open(Consumer)}.
         */
        private static IDialogOpener<Void, String> DIALOG;

        /**
         * Opens an instance of the panel in a dialog.
         * 
         * @param cb
         *           the callback.
         */
        public static void open(Consumer<Optional<String>> callback) {
            if (DIALOG == null)
                DIALOG = ModalDialogCreator.<Void, String, MultiStepDialog4>dialog (new MultiStepDialog4 (), cfg -> {
                    cfg.style (ModalStyle.SEPARATED)
                            .title ("Multistep dialog 4")
                            .type (Type.CENTER)
                            .width (Length.px (500));
                }, b -> {
                    b.cancel ();
                    b.button ((c, btn) -> {
                        btn.reference ("back");
                        btn.label ("Back").icon (FontAwesome.arrowLeft ());
                        btn.left (true);
                        btn.handler (cb -> {
                            if (cb.contents ().prev () == Page.PAGE1)
                                cb.modal ().disable ("back");
                            cb.modal ().hide ("save");
                            cb.modal ().show ("next");
                            cb.fail ();
                        });
                    });
                    b.button ((c, btn) -> {
                        btn.reference ("next");
                        btn.label ("Next").icon (FontAwesome.arrowRight (), true);
                        btn.handler (cb -> {
                            if (cb.contents ().next () == Page.PAGE3) {
                                cb.modal ().show ("save");
                                cb.modal ().hide ("next");
                            }
                            cb.modal ().enable ("back");
                            cb.fail ();
                        });
                    });
                    b.button ((c, btn) -> {
                        btn.reference ("save");
                        btn.label ("Save").icon (FontAwesome.check (), true);
                        btn.handler (cb -> {
                            cb.contents ().process (outcome -> {
                                if (outcome.isPresent()) {
                                    c.accept (outcome);
                                    cb.success ();
                                } else
                                    cb.fail ();
                            });
                        });
                    });
                }).listener (modal -> {
                    modal.disable ("back");
                    modal.show ("next");
                    modal.hide ("save");
                });
            DIALOG.open (null, callback);
        }

        /************************************************************************
         * Construction.
         ************************************************************************/

        public enum Page {
            PAGE1, PAGE2, PAGE3;
        }

        /**
         * The current page.
         */
        private Page page = Page.PAGE1;

        private JQueryElement page1El;

        private JQueryElement page2El;

        private JQueryElement page3El;

        private TextControl value1Ctl;

        private TextControl value2Ctl;

        private TextControl value3Ctl;
        

        @Override
        protected INodeProvider buildNode(Element el) {
            return Wrap.$ (el).$ (root -> {
                root.css (CSS.PADDING, Insets.em (2,1));
                Div.$ (root).$ (page1 -> {
                    Div.$ (page1).$ (top -> {
                        ProgressSequenceCreator.$ (top, cfg -> {
                            cfg.add ("analyse", State.ACTIVE);
                            cfg.add ("validate", State.PENDING);
                            cfg.add ("process", State.PENDING);
                        });
                    });
                    page1.by ("page1"); 
                    P.$ (page1).$ (p -> {
                        p.text ("This is the second step");
                    });
                    page1.insert (value1Ctl = TextControlCreator.build (cfg-> {
                        cfg.placeholder ("Last field");
                    }));
                });
                Div.$ (root).$ (page2 -> {
                    Div.$ (page2).$ (top -> {
                        ProgressSequenceCreator.$ (top, cfg -> {
                            cfg.add ("analyse", State.DONE);
                            cfg.add ("validate", State.ACTIVE);
                            cfg.add ("process", State.PENDING);
                        });
                    });
                    page2.by ("page2"); 
                    P.$ (page2).$ (p -> {
                        p.text ("This is the second step");
                    });
                    page2.insert (value2Ctl = TextControlCreator.build (cfg-> {
                        cfg.placeholder ("Last field");
                    }));
                });
                Div.$ (root).$ (page3 -> {
                    Div.$ (page3).$ (top -> {
                        ProgressSequenceCreator.$ (top, cfg -> {
                            cfg.add ("analyse", State.DONE);
                            cfg.add ("validate", State.DONE);
                            cfg.add ("process", State.ACTIVE);
                        });
                    });
                    page3.by ("page3"); 
                    P.$ (page3).$ (p -> {
                        p.text ("This is the last step");
                    });
                    page3.insert (value3Ctl = TextControlCreator.build (cfg-> {
                        cfg.placeholder ("Last field");
                    }));
                });
            }).build (dom -> {
                page1El = JQuery.$ ((Element) dom.first ("page1"));
                page2El = JQuery.$ ((Element) dom.first ("page2"));
                page3El = JQuery.$ ((Element) dom.first ("page3"));
                refesh ();
            });
        }

        public Page next() {
            if (page == Page.PAGE1)
                page = Page.PAGE2;
            else if (page == Page.PAGE2)
                page = Page.PAGE3;
            refesh ();
            return page;
        }

        public Page prev() {
            if (page == Page.PAGE2)
                page = Page.PAGE1;
            else if (page == Page.PAGE3)
                page = Page.PAGE2;
            refesh ();
            return page;
        }

        protected void refesh() {
            if (page1El == null)
                return;

            page1El.hide ();
            page2El.hide ();
            page3El.hide ();

            if (page == Page.PAGE1)
                page1El.show ();
            else if (page == Page.PAGE2)
                page2El.show ();
            else if (page == Page.PAGE3)
                page3El.show ();
        }


        public void process(Consumer<Optional<String>> callback) {
            callback.accept (Optional.of (value1Ctl.value () + " " + value2Ctl.value () + " " + value3Ctl.value ()));
        }

        @Override
        public void reset() {
            if (value1Ctl != null) {
                value1Ctl.setValue ("");
                value2Ctl.setValue ("");
                value3Ctl.setValue ("");
            }
            page = Page.PAGE1;
            refesh ();
        }
        
    }
}
