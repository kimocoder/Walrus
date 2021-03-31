/*
 * Copyright 2018 Daniel Underhay & Matthew Daley.
 *
 * This file is part of Walrus.
 *
 * Walrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Walrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Walrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.bugfuzz.android.projectwalrus.device;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Pair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public abstract class LineBasedUsbSerialCardDevice extends UsbSerialCardDevice<String> {

    private final String delimiter;
    private final String charsetName;
    private boolean bytewise = false;

    protected LineBasedUsbSerialCardDevice(Context context, UsbDevice usbDevice, String delimiter,
            String charsetName, String status) throws IOException {
        super(context, usbDevice, status);

        this.delimiter = delimiter;
        this.charsetName = charsetName;
    }

    @Override
    protected Pair<String, Integer> sliceIncoming(byte[] in) {
        if (bytewise) {
            if (in.length == 0) {
                return null;
            }
            return new Pair<>(new String(new byte[]{in[0]}), 1);
        }
        String string;
        try {
            string = new String(in, charsetName);
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        int index = string.indexOf(delimiter);
        if (index == -1) {
            return null;
        }

        // TODO FIXME: this is assuming 1 char == 1 byte
        return new Pair<>(string.substring(0, index), index + delimiter.length());
    }

    @Override
    protected byte[] formatOutgoing(String out) {
        if (bytewise) {
            return new byte[] { (byte) out.charAt(0) };
        }
        try {
            return (out + delimiter).getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    protected void setBytewise(boolean bytewise) {
        this.bytewise = bytewise;
    }

    protected void sendByte(byte b) {
        try {
            send(new String(new byte[]{b}, "ISO-8859-1"));
        } catch (UnsupportedEncodingException e) {
        }
    }

    protected byte receiveByte(long timeout) {
        return receive(timeout).getBytes()[0];
    }
}
