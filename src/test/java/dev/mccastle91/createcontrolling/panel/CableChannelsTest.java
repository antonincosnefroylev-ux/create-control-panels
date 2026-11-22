package dev.mccastle91.createcontrolling.panel;

import dev.mccastle91.createcontrolling.compat.drivebysable.CableChannels;
import dev.mccastle91.createcontrolling.panel.element.KnobElement;
import dev.mccastle91.createcontrolling.panel.element.LabelElement;
import dev.mccastle91.createcontrolling.panel.element.LampElement;
import dev.mccastle91.createcontrolling.panel.element.PanelElement;
import dev.mccastle91.createcontrolling.panel.element.PanelElements;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Cable channel names for console parts, and how a wired receiver combines its two signals. */
class CableChannelsTest {

    @Test
    void channelsNameWirablePartsAndStayPut() {
        PanelLayout layout = new PanelLayout();
        KnobElement knob = PanelElements.KNOB.create();
        LampElement lamp = PanelElements.LAMP.create();
        LabelElement label = PanelElements.LABEL.create();
        knob.setPos(1, 1);
        lamp.setPos(8, 1);
        label.setPos(1, 10);
        layout.addElement(knob);
        layout.addElement(label);
        layout.addElement(lamp);

        Map<String, PanelElement> channels = CableChannels.of(layout);
        assertEquals(List.of(knob, lamp), List.copyOf(channels.values()), "labels carry no cable");
        String knobChannel = CableChannels.of(layout, knob.id());
        String lampChannel = CableChannels.of(layout, lamp.id());
        assertTrue(knobChannel.matches("Knob [0-9A-F]{4}"), knobChannel);
        assertTrue(lampChannel.matches("Indicator Lamp [0-9A-F]{4}"), lampChannel);
        assertNull(CableChannels.of(layout, label.id()));

        // editing the console around a part never renames it
        knob.setPos(5, 5);
        KnobElement second = PanelElements.KNOB.create();
        second.setPos(1, 6);
        layout.addElement(second);
        layout.removeElement(lamp.id());
        assertEquals(knobChannel, CableChannels.of(layout, knob.id()));
        assertFalse(CableChannels.of(layout).containsKey(lampChannel));
        assertEquals(2, CableChannels.of(layout).size());
    }

    @Test
    void wiredReceiverShowsTheStrongerSignal() {
        LampElement lamp = PanelElements.LAMP.create();
        assertTrue(lamp.receiveWired(6));
        assertEquals(6, lamp.displayStrength());
        assertFalse(lamp.receiveStrength(3), "a weaker frequency signal changes nothing shown");
        assertTrue(lamp.receiveStrength(9));
        assertEquals(9, lamp.displayStrength());
        assertFalse(lamp.receiveWired(0), "the frequency's 9 still shows");
        assertTrue(lamp.receiveStrength(0));
        assertEquals(0, lamp.displayStrength());

        KnobElement knob = PanelElements.KNOB.create();
        assertFalse(knob.receiveWired(15), "controls take no cable signal");
    }
}
