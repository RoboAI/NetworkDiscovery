package com.example.networkdiscovery;

public class GlobalDefines {
    public static final String TAG = "ABCD";

    public static final int DEFAULT_MASTER_PORT = 54321;
    public static final int DEFAULT_UDP_PORT = 55056;

    public static final int DEFAULT_CLIENT_PORT = 55059;

    public final static int PASSWORD_LENGTH = 10;

    public final static String START_TAG = "!";

    public final static String[] CLIENT_PROTOCOL_STAGES =
            {"is_anyone_there?",
                    "hi_from_client"};

    public final static String[] SERVER_PROTOCOL_STAGES =
            { "app_server_uri:"};

    public static String globalMyIP = "";
}
