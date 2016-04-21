package com.NccAPI;

import com.NccAPI.AstraManager.AstraManagerImpl;
import com.NccAPI.AstraManager.AstraManagerService;
import com.NccAPI.NAS.NasService;
import com.NccAPI.NAS.NasServiceImpl;
import com.NccAPI.Pools.PoolsService;
import com.NccAPI.Pools.PoolsServiceImpl;
import com.NccAPI.Sessions.SessionsService;
import com.NccAPI.Sessions.SessionsServiceImpl;
import com.NccAPI.UserAccounts.AccountsService;
import com.NccAPI.UserAccounts.AccountsServiceImpl;
import com.NccAPI.Users.UsersService;
import com.NccAPI.Users.UsersServiceImpl;
import com.NccAPI.Views.ViewsService;
import com.NccAPI.Views.ViewsServiceImpl;
import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.googlecode.jsonrpc4j.ProxyUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

public class NccAPI {

    private Server apiServer;
    private static Logger logger = Logger.getLogger(NccAPI.class);

    public NccAPI() {

        class NccAPIHandler extends AbstractHandler {

            class CompositeServer extends JsonRpcServer {

                public CompositeServer(Object handler) {
                    super(handler);
                }

                public void handleCORS(HttpServletRequest request, HttpServletResponse response)
                        throws IOException {

                    // set response type
                    response.setContentType(JSONRPC_RESPONSE_CONTENT_TYPE);

                    // setup streams
                    InputStream input;
                    OutputStream output	= response.getOutputStream();

                    // POST
                    if (request.getMethod().equals("POST")) {
                        input = request.getInputStream();
                        response.addHeader("Access-Control-Allow-Origin", "*");

                    } else if (request.getMethod().equals("OPTIONS")){
                        response.addHeader("Access-Control-Allow-Headers", "Content-Type, POST");
                        response.addHeader("Access-Control-Allow-Origin", "*");

                        output.flush();
                        return;
                    // GET
                    } else if (request.getMethod().equals("GET")) {
                        input = createInputStream(
                                request.getParameter("method"),
                                request.getParameter("id"),
                                request.getParameter("params"));

                        // invalid request
                    } else {
                        throw new IOException(
                                "Invalid request method, only POST and GET is supported");
                    }

                    // service the request
                    //fix to set HTTP status correctly
                    int result = handle(input, output);
                    if(result != 0){
                        if (result == -32700 || result == -32602 || result == -32603
                                || (result <= -32000 && result >= -32099)) {
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        } else if (result == -32600) {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        } else if (result == -32601) {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        }
                    }
                    //fix to not flush within handle() but outside so http status code can be set
                    output.flush();
                }
            }

            private CompositeServer compositeServer;

            private UsersService userService;
            private AccountsService accountsService;
            private PoolsService poolsService;
            private SessionsService sessionsService;
            private NasService nasService;
            private ViewsService viewsService;
            private AstraManagerService astraManagerService;

            private Object compositeService;

            public NccAPIHandler() {
                userService = new UsersServiceImpl();
                accountsService = new AccountsServiceImpl();
                poolsService = new PoolsServiceImpl();
                sessionsService = new SessionsServiceImpl();
                nasService = new NasServiceImpl();
                viewsService = new ViewsServiceImpl();
                astraManagerService = new AstraManagerImpl();

                compositeService = ProxyUtil.createCompositeServiceProxy(
                        this.getClass().getClassLoader(),
                        new Object[]{
                                userService,
                                accountsService,
                                poolsService,
                                sessionsService,
                                nasService,
                                viewsService,
                                astraManagerService
                        },
                        new Class<?>[]{
                                UsersService.class,
                                AccountsService.class,
                                PoolsService.class,
                                SessionsService.class,
                                NasService.class,
                                ViewsService.class,
                                AstraManagerService.class
                        },
                        true);

                compositeServer = new CompositeServer(compositeService);
            }

            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

                logger.debug("API request: " + request.getServletPath());

                switch (target) {
                    case "/api":
                        compositeServer.handleCORS(request, response);
                        break;
                    default:
                        break;
                }
            }
        }

        apiServer = new Server(8032);
        apiServer.setHandler(new NccAPIHandler());
    }

    public void start() {
        try {
            apiServer.start();
            apiServer.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            apiServer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean checkKey(String apiKey){
        if(apiKey.equals("CtrhtnT,fnmRfrjq")) return true;
        return false;
    }

    public boolean checkPermission(String apiKey, String permission){
        String login = "admin";
        String password = "CtrhtnysqGfhjkm";

        String hash = DigestUtils.md5Hex(DigestUtils.md5Hex(login).concat(password));

        if(apiKey.equals(hash)) return true;

        return false;
    }
}
