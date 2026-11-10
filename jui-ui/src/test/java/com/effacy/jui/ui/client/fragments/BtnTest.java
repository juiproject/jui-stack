package com.effacy.jui.ui.client.fragments;

import org.junit.jupiter.api.Test;

import com.effacy.jui.core.client.test.AbstractJUITest;

public class BtnTest extends AbstractJUITest {

    @Test
    public void button_with_icon_and_label() {
        build(Btn.$("Save").icon("fa-save"))
            //.print()
            .htmlEquals("""
                <div>
                    <button class="fragment">
                        <em class="fa-save"></em>
                        <span class="runningpart">
                            <em class="fas fa-spin fa-spinner"></em>
                        </span>
                        <span class="label">Save</span>
                    </button>
                </div>
                """)
            .child("button.fragment", c1 -> {
                c1.exists().tagEquals("button").child("em.fa-save", c2 -> {
                    c2.exists().tagEquals("em");
                });
                c1.child("span.runningpart", c2 -> {
                    c2.exists().tagEquals("span").child("em.fa-spinner", c3 -> {
                        c3.exists().tagEquals("em").hasClass("fa-spin");
                    });
                });
            })
            .child("button.fragment", c1 -> {
                c1.child("span.label", c2 -> {
                    c2.exists().tagEquals("span").textEquals("Save");
                });
            });
    }

    @Test
    public void disabled_button_cannot_be_clicked() {
        // The disabled property is set on the button itself (not just styled).
        build(Btn.$("Save").disable(true))
            .child("button.fragment", c -> {
                c.exists().tagEquals("button").hasClass("disabled").propertyEquals("disabled", true);
            });
    }

    @Test
    public void disabled_through_attr() {
        // Any present value of the attribute disables the button.
        build(Btn.$("Save").attr("disabled", "disabled"))
            .child("button.fragment", c -> {
                c.exists().propertyEquals("disabled", true);
            });
        build(Btn.$("Save").attr("disabled", "true"))
            .child("button.fragment", c -> {
                c.exists().propertyEquals("disabled", true);
            });
    }

    @Test
    public void enabled_button() {
        build(Btn.$("Save").disable(false))
            .child("button.fragment", c -> {
                c.exists().propertyEquals("disabled", null);
            });
    }
}
