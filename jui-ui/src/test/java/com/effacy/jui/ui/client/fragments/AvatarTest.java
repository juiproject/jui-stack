package com.effacy.jui.ui.client.fragments;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.ui.client.fragments.Avatar.AvatarFragment;

public class AvatarTest {

    @Test
    public void configuration() {
        LocalAvatarFragment f = new LocalAvatarFragment("aaa");
        Assertions.assertEquals("aaa", f.href())
    }

    @Test
    public void configuration_initials() {
        LocalAvatarFragment f = new LocalAvatarFragment("");

        f.initials(null);
        Assertions.assertEquals(null, f.initials());

        f.initials("  ");
        Assertions.assertEquals(null, f.initials());

        f.initials("A");
        Assertions.assertEquals("A", f.initials());

        f.initials("AB");
        Assertions.assertEquals("AB", f.initials());

        f.initials(" AB  ");
        Assertions.assertEquals("AB", f.initials());

        f.initials("A B");
        Assertions.assertEquals("AB", f.initials());

        f.initials("Aaa Bbb");
        Assertions.assertEquals("AB", f.initials());

        f.initials("Aaa Cccc Bbbb");
        Assertions.assertEquals("AB", f.initials());
    }

    public class LocalAvatarFragment extends AvatarFragment {

        public LocalAvatarFragment(String href) {
            super(href);
        }

        public String href() {
            return href;
        }

        public String initials() {
            return initials;
        }
        
    }
}
