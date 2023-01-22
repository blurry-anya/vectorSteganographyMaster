package com.diploma.stegovector.interfaces;


import com.diploma.stegovector.objects.Message;

import java.util.List;

public interface Stegotransformation {

    List<String> encode(Message message);

    Message decode(double error);
}
