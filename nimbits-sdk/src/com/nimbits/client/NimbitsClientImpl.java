package com.nimbits.client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.nimbits.client.enums.Action;
import com.nimbits.client.exception.NimbitsException;
import com.nimbits.client.model.Const;
import com.nimbits.client.model.category.Category;
import com.nimbits.client.model.category.CategoryName;
import com.nimbits.client.model.category.impl.CategoryModel;
import com.nimbits.client.model.common.CommonFactoryLocator;
import com.nimbits.client.model.email.EmailAddress;
import com.nimbits.client.model.point.Point;
import com.nimbits.client.model.point.PointModel;
import com.nimbits.client.model.point.PointName;
import com.nimbits.client.model.user.User;
import com.nimbits.client.model.value.Value;
import com.nimbits.client.model.value.ValueModel;
import com.nimbits.client.model.value.ValueModelFactory;
import com.nimbits.exceptions.GoogleAuthenticationException;
import com.nimbits.server.gson.GsonFactory;
import com.nimbits.server.http.HttpCommonFactory;
import com.nimbits.user.GoogleAuthentication;
import com.nimbits.user.GoogleUser;
import com.nimbits.user.NimbitsUser;
import org.apache.commons.lang3.*;
import org.apache.http.cookie.Cookie;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class NimbitsClientImpl implements NimbitsClient {


    final private static Gson gson = GsonFactory.getInstance();

    private final GoogleAuthentication G;
    private final String host;
    private Cookie authCookie;
    private final NimbitsUser nimbitsUser;
    private final GoogleUser googleUser;
//    private NimbitsClientImpl() {
//    }

    public NimbitsClientImpl(final NimbitsUser n, final String hostUrl) {
        this.host = hostUrl;
        nimbitsUser = n;
        googleUser = null;
        G = GoogleAuthentication.getNewGoogleAuthentication();
        G.setEmail(n.getEmailAddress());
        G.setSecret(n.getNimbitsSecretKey());
    }

    public NimbitsClientImpl(final GoogleUser g, final String hostUrl) throws NimbitsException {
        this.host = hostUrl;
        nimbitsUser = null;
        googleUser = g;
        G = GoogleAuthentication.getNewGoogleAuthentication();
        G.setEmail(g.getGoogleEmailAddress());
        G.Connect(hostUrl, g.getGoogleEmailAddress(), g.getGooglePassword());

    }

    public String getHost() {
        return host;
    }

    public NimbitsClientImpl(final String token, final EmailAddress email, final String hostUrl) throws NimbitsException, GoogleAuthenticationException {
        this.host = hostUrl;
        G = GoogleAuthentication.getNewGoogleAuthentication();
        G.setEmail(email);
        if (!email.getValue().equals(Const.TEST_ACCOUNT)) {

            authCookie = G.ConnectAuth(token, hostUrl);

        }
        nimbitsUser = null;
        googleUser = null;


    }

    public Cookie getAuthCookie() {
        return authCookie;
    }


    public boolean isLoggedIn() throws NimbitsException {

        return Boolean.parseBoolean(doGGet(host + Const.PATH_AUTHTEST_SERVICE, ""));

    }


    public List<User> getUsers() throws NimbitsException {
        String u = host + Const.PATH_USER_SERVICE;
        String params = "action=download";
        String result = doGGet(u, params);

        return gson.fromJson(result, GsonFactory.userListType);


    }

    public String getChart(final String points, final int count) throws NimbitsException {
        final String u = host + Const.Path_CHART_API;
        String params = null;
        final String result;
        try {
            params = "count=10&points=" + URLEncoder.encode(points, Const.CONST_ENCODING) + "&chxt=y&chxp=1,75,100&cht=lc&chco=76A4FB&chls=2.0&chs=300x200";
        } catch (UnsupportedEncodingException e1) {

            e1.printStackTrace();
        }


        result = doGGet(u, params);


        return result;
    }

    public String getChartURL(final String points, final int count, final String additionalParams) {
        final String u = host + Const.Path_CHART_API;
        String params = null;

        try {
            params = "count=10&points=" + URLEncoder.encode(points, Const.CONST_ENCODING) + "&" + additionalParams;

        } catch (UnsupportedEncodingException e1) {

            e1.printStackTrace();
        }


        return u + "?" + params;
    }

    public void deletePoint(final PointName pointName) {
        final String u = host + Const.PATH_POINT_SERVICE;
        try {
            String params = "name=" + URLEncoder.encode(pointName.getValue(), Const.CONST_ENCODING) + "&action=delete";
            doGPost(u, params);
        } catch (UnsupportedEncodingException ignored) {

        }

    }

    public void deletePoint(final String pointName) {
        final PointName pointName1 = CommonFactoryLocator.getInstance().createPointName(pointName);
        final String u = host + Const.PATH_POINT_SERVICE;
        try {
            String params = "name=" + URLEncoder.encode(pointName1.getValue(), Const.CONST_ENCODING) + "&action=delete";
            doGPost(u, params);
        } catch (UnsupportedEncodingException ignored) {

        }

    }

    public Value recordValue(final PointName pointName,
                             final double value,
                             final Date timestamp) {
        final String u = host + Const.PATH_CURRENT_VALUE;
        String params;
        try {
            params = new StringBuilder().append(Const.PARAM_POINT).append("=").append(URLEncoder.encode(pointName.getValue(), Const.CONST_ENCODING)).append("&").append(Const.PARAM_TIMESTAMP).append("=").append(timestamp.getTime()).append("&").append(Const.PARAM_VALUE).append("=").append(value).toString();
        } catch (UnsupportedEncodingException ignored) {
            params = null;
        }

        String json = doGPost(u, params);
        if (StringUtils.isEmpty(json)) {
            json = doGPost(u, params); //retry
        }
        return gson.fromJson(json, ValueModel.class);
    }

    public Value recordValue(final PointName pointName,
                             final double value) {
        return recordValue(pointName, value, new Date());
    }

    public Value recordValue(final String pointName,
                             final double value) {

        final PointName pointName1 = CommonFactoryLocator.getInstance().createPointName(pointName);


        return recordValue(pointName1, value, new Date());
    }


    public Value recordValueWithGet(final PointName pointName, final double value, final Date timestamp) throws IOException, NimbitsException {
        final String u = host + Const.PATH_CURRENT_VALUE;
        String params = "point=" + URLEncoder.encode(pointName.getValue(), Const.CONST_ENCODING) +
                "&timestamp=" + timestamp.getTime() +
                "&value=" + value;

        String json = doGGet(u, params);
        System.out.println(json);
        double d = Double.valueOf(json);
        return ValueModelFactory.createValueModel(d);

        //return gson.fromJson(json, ValueModel.class);

    }

    @Override
    public Value recordValue(String pointName, double value, Date timestamp) {
        PointName pointName1 = CommonFactoryLocator.getInstance().createPointName(pointName);
        return recordValue(pointName1, value, timestamp);
    }

    public String recordBatch(String params) {
        String u = host + Const.PATH_BATCH_SERVICE;

        return doGPost(u, params);
    }


    public Value recordValue(PointName pointName, Value v) throws IOException {
        String u = host + Const.PATH_CURRENT_VALUE;
        String json = gson.toJson(v, ValueModel.class);
        String params = Const.PARAM_TIMESTAMP +
                "=" + v.getTimestamp().getTime() +
                "&" + Const.PARAM_POINT + "=" +
                URLEncoder.encode(pointName.getValue(), Const.CONST_ENCODING) +
                "&" + Const.PARAM_JSON + "=" + URLEncoder.encode(json, Const.CONST_ENCODING);
        String result = doGPost(u, params);
        return gson.fromJson(result, ValueModel.class);

    }

    /**
     * Add a new Category
     *
     * @param categoryName the name of the new category
     * @throws UnsupportedEncodingException
     */
    public Category addCategory(final CategoryName categoryName) throws UnsupportedEncodingException {

        final String u = host + Const.PATH_CATEGORY_SERVICE;
        final String params = "name=" + URLEncoder.encode(categoryName.getValue(), Const.CONST_ENCODING);
        final String result = doGPost(u, params);
        return gson.fromJson(result, CategoryModel.class);


    }

    public String deleteCategory(final CategoryName categoryName) {
        String retVal = "";
        try {
            String u = host + Const.PATH_CATEGORY_SERVICE;
            String params;
            params = Const.PARAM_ACTION
                    + "=" + Action.delete.name()
                    + "&" + Const.PARAM_NAME
                    + "=" + URLEncoder.encode(categoryName.getValue(), Const.CONST_ENCODING);
            retVal = doGPost(u, params);
        } catch (UnsupportedEncodingException ignored) {

        }
        return retVal;


    }

    public Point addPoint(final CategoryName categoryName, final PointName pointName) {
        Point point = null;

        try {
            final String u = host + Const.PATH_POINT_SERVICE;
            final String params = Const.PARAM_NAME + "=" +
                    URLEncoder.encode(pointName.getValue(), Const.CONST_ENCODING) +
                    "&" + Const.PARAM_CATEGORY + "=" + URLEncoder.encode(categoryName.getValue(), Const.CONST_ENCODING);
            String json = doGPost(u, params);
            point = gson.fromJson(json, PointModel.class);
        } catch (JsonSyntaxException ignored) {

        } catch (UnsupportedEncodingException ignored) {

        }
        return point;


    }

    @Override
    public Point addPoint(final String pointName) {
        final CategoryName categoryName = CommonFactoryLocator.getInstance().createCategoryName(Const.CONST_HIDDEN_CATEGORY);
        final PointName pointName1 = CommonFactoryLocator.getInstance().createPointName(pointName);
        return addPoint(categoryName, pointName1);

    }

    public Point getPoint(final PointName pointName) throws NimbitsException {
        Point retObj = null;

        try {
            final String u = host + Const.PATH_POINT_SERVICE;
            final String params = Const.PARAM_NAME + "=" + URLEncoder.encode(pointName.getValue(), Const.CONST_ENCODING);
            String json = doGGet(u, params);
            retObj = gson.fromJson(json, PointModel.class);

        } catch (UnsupportedEncodingException ignored) {

        } catch (IOException ignored) {

        }
        return retObj;


    }

    @Override
    public Point updatePoint(final Point p) {
        Point ret = null;

        try {
            String u = host + Const.PATH_POINT_SERVICE;
            String params;
            String json = gson.toJson(p);
            params = Const.PARAM_JSON + "=" + URLEncoder.encode(json, Const.CONST_ENCODING) +
                    "&" + Const.PARAM_ACTION + "=" + Const.ACTION_UPDATE;
            String response = doGPost(u, params);
            if (response != null) {
                ret = gson.fromJson(response, PointModel.class);
            }
        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        }
        return ret;

    }


    public Point addPoint(final Point p, final CategoryName categoryName) {
        Point retObj = null;
        final String newPointJson = gson.toJson(p);
        try {
            String u = host + Const.PATH_POINT_SERVICE;
            String params;
            params = Const.PARAM_JSON + "=" + URLEncoder.encode(newPointJson, Const.CONST_ENCODING);
            params += "&" + Const.PARAM_CATEGORY + "=" +
                    URLEncoder.encode(categoryName.getValue(), Const.CONST_ENCODING);
            String result = doGPost(u, params);
            retObj = gson.fromJson(result, PointModel.class);
        } catch (UnsupportedEncodingException e) {


        }
        return retObj;

    }

    public List<Category> getCategories(final boolean includePoints, final boolean includeDiagrams) throws NimbitsException {
        final String u = host + Const.PATH_CATEGORY_SERVICE;
        String params = "";
        //  final String categories = doGGet(u, params);


        if (includePoints) {
            params = Const.PARAM_INCLUDE_POINTS + "=" + Const.WORD_TRUE;
        }
        if (includeDiagrams) {
            params += "&" + Const.PARAM_INCLUDE_DIAGRAMS + "=" + Const.WORD_TRUE;
        }

        final String json = doGGet(u, params);


        List<Category> retObj = gson.fromJson(json, GsonFactory.categoryListType);


        return retObj;


    }

    public Category getCategory(final CategoryName categoryName, final boolean includePoints, final boolean includeDiagrams) throws NimbitsException {
        Category c = null;
        final String u = host + Const.PATH_CATEGORY_SERVICE;
        String params = Const.PARAM_NAME + "=" + categoryName.getValue();

        if (includePoints) {
            params += "&" + Const.PARAM_INCLUDE_POINTS + "=" + Const.WORD_TRUE;
        }
        if (includeDiagrams) {
            params += "&" + Const.PARAM_INCLUDE_DIAGRAMS + "=" + Const.WORD_TRUE;
        }

        final String json = doGGet(u, params);

        c = gson.fromJson(json, CategoryModel.class);
        //  if (!(json.trim().length() == 0)) {


//            if (c.getJsonPointCollection() != null) {
//                List<Point> points = gson.fromJson(c.getJsonPointCollection(), GsonFactory.categoryListType);
//                c.setPoints(points);
//
//            }
//            if (c.getJsonDiagramCollection() != null) {
//                List<Diagram> diagrams = gson.fromJson(c.getJsonDiagramCollection(), GsonFactory.diagramListType);
//                c.setDiagrams(diagrams);
//
//            }

        //  }


        return c;


    }

    public String currentValue(final PointName pointName) throws IOException, NimbitsException {
        String u = host + Const.PATH_CURRENT_VALUE;
        String params = Const.PARAM_POINT + "=" + URLEncoder.encode(pointName.getValue(), Const.CONST_ENCODING);
        return doGGet(u, params);

    }

    public Object getCurrentDataObject(final PointName pointName, Class<?> cls) {
        Value value = getCurrentRecordedValue(pointName);
        if (value.getData() != null) {
            return gson.fromJson(value.getData(), cls);
        } else {
            return null;
        }
    }

    @Override
    public Value recordDataObject(PointName pointName, Object object, Class<?> cls) throws NimbitsException {
        Value value = ValueModelFactory.createValueModel(0.0, 0.0, 0.0, new Date(), 0, cls.getName(), gson.toJson(object));
        try {
            return recordValue(pointName, value);
        } catch (IOException e) {
            throw new NimbitsException(e.getMessage());
        }


    }

    @Override
    public Value recordDataObject(PointName pointName, Object object, Class<?> cls, double latitude, double longitude, double value) throws NimbitsException {
        Value vx = ValueModelFactory.createValueModel(latitude, longitude, value, new Date(), 0, cls.getName(), gson.toJson(object));
        try {
            return recordValue(pointName, vx);
        } catch (IOException e) {
            throw new NimbitsException(e.getMessage());
        }


    }

    public Value getCurrentRecordedValue(final PointName pointName) {
        Value retObj = null;
        String u = host + Const.PATH_CURRENT_VALUE;
        String params;
        String json;

        try {
            params = Const.PARAM_POINT + "=" + URLEncoder.encode(pointName.getValue(), Const.CONST_ENCODING) + "&format=json";
            json = doGGet(u, params);

            if (json != null) {
                retObj = gson.fromJson(json, ValueModel.class);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }


        return retObj;


    }

    public List<Value> getSeries(final String pointName, final int count) throws NimbitsException {
        PointName pointName1 = CommonFactoryLocator.getInstance().createPointName(pointName);
        return getSeries(pointName1, count);

    }

    public List<Value> getSeries(final PointName pointName, final int count) throws NimbitsException {
        List<Value> retObj = null;

        String result;

        final String destUrl = host + Const.PATH_SERIES_SERVICE;
        String params;
        try {
            params = Const.PARAM_COUNT + "=" + count + "&" + Const.PARAM_POINT + "=" + URLEncoder.encode(pointName.getValue(), Const.CONST_ENCODING);
            result = doGGet(destUrl, params);

            retObj = gson.fromJson(result, GsonFactory.valueListType);

        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }

        return retObj;

    }

    public List<Value> getSeries(final PointName pointName, final Date startDate, final Date endDate) throws NimbitsException {
        final List<Value> retObj = new ArrayList<Value>();


        String result;

        String destUrl = host + Const.PATH_SERIES_SERVICE;
        String params;
        int seg = 0;

        try {
            while (true) {
                params = "seg=" + seg + "&sd=" + startDate.getTime() + "&ed=" + endDate.getTime() + "&" + Const.PARAM_POINT + "=" + URLEncoder.encode(pointName.getValue(), Const.CONST_ENCODING);
                result = doGGet(destUrl, params);
                List<Value> r = gson.fromJson(result, GsonFactory.valueListType);

                if (r == null || r.size() == 0) {
                    break;
                } else {
                    retObj.addAll(r);
                }
                seg += 1000;
            }

        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }

        return retObj;

    }

    @Override
    public void downloadSeries(final PointName pointName, final Date startDate, final Date endDate, final String filename) throws IOException, NimbitsException {
        final List<Value> r = getSeries(pointName, startDate, endDate);

        String json = gson.toJson(r, GsonFactory.valueListType);

        Writer out;

        out = new OutputStreamWriter(new FileOutputStream(filename));
        out.write(json);
        out.close();


    }

    @Override
    public List<Value> loadSeriesFile(final String fileName) throws IOException {

        final StringBuilder sb = new StringBuilder();
        final BufferedReader in = new BufferedReader(new FileReader(fileName));
        String str;

        while ((str = in.readLine()) != null) {
            sb.append(str);
        }
        in.close();
        return gson.fromJson(sb.toString(), GsonFactory.valueListType);

    }

    @Override
    public byte[] getChartImage(final String baseURL, final String params) {
        final String url = baseURL + Const.PATH_CHART_SERVICE;
        return doGGetBinary(url, params);

    }


//    public byte[] getBinaryFile(String postUrl, String params) throws Exception {
//        byte[] retObj;
//        int c;
//
//        URL url = new URL(postUrl + "?" + params);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setDoOutput(true);
//        connection.setRequestMethod("GET");
//
//        if (G != null) {
//
//            try {
//                if (G.getAuthCookie() != null) {
//                    connection.addRequestProperty("Cookie", G.getAuthCookie().getValue() + "=" + G.getAuthCookie().getValue());
//                }
//
//            } catch (Exception e) {
//
//            }
//            params += getAuthParams();
//
//        }
//
//        DataInputStream in = new DataInputStream(connection.getInputStream());
//        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
//
//
//        while ((c = in.read()) != -1) {
//            byteArrayOut.write(c);
//        }
//        retObj = byteArrayOut.toByteArray();
//
//
//        return retObj;
//
//    }

    private String getAuthParams() {

        final StringBuilder b = new StringBuilder();
        b.append("&" + Const.PARAM_EMAIL + "=").append(G.getEmail().getValue());
        if (G.getSecret() != null) {
            b.append("&" + Const.PARAM_SECRET + "=").append(G.getSecret());
        }

        return b.toString();


    }

    private String doGGet(final String url, final String params) throws NimbitsException {
        String cookie = null;
        String postParams = params;
        if (G != null) {
            if (G.getAuthCookie() != null) {
                cookie = G.getAuthCookie().getName() + "=" + G.getAuthCookie().getValue();
            }
            postParams += getAuthParams();

        }

        return HttpCommonFactory.getInstance().doGet(url, postParams, cookie);


    }

    private byte[] doGGetBinary(final String url, final String params) {
        String cookie = null;
        String postParams = params;
        if (G != null) {
            if (G.getAuthCookie() != null) {
                cookie = G.getAuthCookie().getName() + "=" + G.getAuthCookie().getValue();
            }
            postParams += getAuthParams();

        }
        try {
            return HttpCommonFactory.getInstance().doGetBytes(url, postParams, cookie);
        } catch (NimbitsException e) {
            return null;
        } catch (Exception e) {
            return null;
        }

        //doPost(u, params, cookie);

    }

    private String doGPost(final String url, final String params) {
        String cookie = null;
        String postParams = params;
        if (G != null) {

            if (G.getAuthCookie() != null) {
                cookie = G.getAuthCookie().getName() + "=" + G.getAuthCookie().getValue();
                //  connection.addRequestProperty(Const.WORD_COOKIE, );
            }

            postParams += getAuthParams();

        }
        try {
            return HttpCommonFactory.getInstance().doPost(url, postParams, cookie);
        } catch (NimbitsException e) {
            e.printStackTrace();
            return null;
        }

        //doPost(u, params, cookie);

    }

    public NimbitsUser getNimbitsUser() {
        return nimbitsUser;
    }

    public GoogleUser getGoogleUser() {
        return googleUser;
    }

}
