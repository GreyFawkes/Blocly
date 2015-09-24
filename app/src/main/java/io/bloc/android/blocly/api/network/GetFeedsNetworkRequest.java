package io.bloc.android.blocly.api.network;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Administrator on 9/22/2015.
 */
public class GetFeedsNetworkRequest extends NetworkRequest {

    private static final String TAG = "getFeedsNetworkRequest";

    String [] feedUrls;

    public GetFeedsNetworkRequest(String... feedUrls) {
        this.feedUrls = feedUrls;
    }

    @Override
    public Object performRequest() {
        for(String feedUrlString : feedUrls) {
            InputStream inputStream = openStream(feedUrlString);
            if(inputStream == null) {
                return null;
            }
            try {
//                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//
//                String line = bufferedReader.readLine();
//                while (line != null) {
//                    Log.v(getClass().getSimpleName(), "Line: " + line);
//                    line = bufferedReader.readLine();
//                }
//
//                bufferedReader.close();

                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = builder.parse(inputStream);
                logNumberOfItems(document);

            } catch (IOException e) {
                e.printStackTrace();
                setErrorCode(ERROR_IO);
                return null;
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
                setErrorCode(ERROR_IO);
                return null;
            } catch (SAXException e) {
                e.printStackTrace();
                setErrorCode(ERROR_IO);
                return null;
            }




        }

        return null;
    }


    private void logNumberOfItems(Document doc) {
        NodeList elements = doc.getElementsByTagName("item");
        Log.i(TAG, "number of items: " + elements.getLength());
    }
}
