package makadown.com.restservice.webservices;

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import makadown.com.restservice.Constants;

/**
 * Created by usuario on 23/05/2017.
 */

public class WebServiceUtils
{
    private static final String TAG = WebServiceUtils.class.getName();
    public enum METHOD{

        POST, GET , DELETE
    }

    public static JSONObject requestJSONObject(String serviceUrl, METHOD method,
                                               ContentValues headerValues,
                                               boolean hasAuthorizarion)
    {
        return requestJSONObject( serviceUrl, method, headerValues, null, null, hasAuthorizarion);
    }

    public static JSONObject requestJSONObject(String serviceUrl, METHOD method,
                                               ContentValues urlValues,
                                               ContentValues bodyValues)
    {
        return requestJSONObject(serviceUrl, method, null, urlValues, bodyValues, false);
    }

    public static JSONObject requestJSONObject( String serviceUrl, METHOD method,
                              ContentValues headerValues,
                              ContentValues urlValues,
                              ContentValues bodyValues,
                              boolean hasAuthorizarion)
    {
        HttpURLConnection urlConnection = null;
        try{
            if ( urlValues != null )
            {
                serviceUrl = addParametersToUrl(serviceUrl, urlValues);
            }

            URL urlToRequest = new URL( serviceUrl );
            urlConnection = (HttpURLConnection) urlToRequest.openConnection();
            urlConnection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout( Constants.READ_TIMEOUT );
            urlConnection.setRequestMethod( method.toString() );
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            if(hasAuthorizarion)
            {
                addBasicAuthentication(urlConnection);
            }

            if(headerValues!= null)
            {
                Uri.Builder builder = new Uri.Builder();
                for (String key : headerValues.keySet() )
                {
                    builder.appendQueryParameter(key, headerValues.getAsString(key) );
                }
                String query = builder.build().getEncodedQuery();
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(os, "UTF-8") );
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
            }

            if ( bodyValues != null  )
            {
                JSONObject jsonObject = new JSONObject();
                for ( String key : bodyValues.keySet() )
                {
                    jsonObject.put(key, bodyValues.getAsString(key));
                }
                String str = jsonObject.toString();
                urlConnection.setRequestProperty( "Content-Type", "application/json" );
                urlConnection.setRequestProperty( "Accept", "application/json" );
                OutputStreamWriter osw = new OutputStreamWriter(urlConnection.getOutputStream());
                osw.write(str);
                osw.flush();
                osw.close();
            }

            int statusCode = urlConnection.getResponseCode();
            if(statusCode== HttpURLConnection.HTTP_UNAUTHORIZED)
            {
                Log.d(TAG,"Acceso no autorizado!");
            }
            else if ( statusCode != HttpURLConnection.HTTP_OK )
            {
                Log.d(TAG,"URL Response Error");
            }

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return new JSONObject(convertInputStreamToStream(in) );
        }
        catch (MalformedURLException e ){
            Log.d(TAG, e.getMessage());
        }
        catch (SocketTimeoutException ste ){
            Log.d(TAG, ste.getMessage());
        }
        catch (IOException ioe ){
            Log.d(TAG, ioe.getMessage());
        }
       /* catch (JSONException jsone ){
            Log.d(TAG, jsone.getMessage());
        }*/
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            if ( urlConnection!= null )
            {
                 urlConnection.disconnect();
            }
        }
        return null;
    }

    private static String convertInputStreamToStream(InputStream inputStream)
    {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String responseText;
        try
        {
            while( ( responseText  = bufferedReader.readLine() ) != null )
            {
                stringBuilder.append(responseText);
            }
        }
        catch(IOException exp)
        {
            Log.d(TAG, "IOException en convertInputStreamToStream");
        }

        return stringBuilder.toString();
    }

    private static String addParametersToUrl(String url, ContentValues urlValues)
    {
        StringBuffer stringBuffer = new StringBuffer(url);
        stringBuffer.append("?");
        for ( String key : urlValues.keySet() )
        {
            stringBuffer.append(key);
            stringBuffer.append("=");
            stringBuffer.append(urlValues.getAsString(key));
            stringBuffer.append("&");
        }

        stringBuffer.replace(stringBuffer.length()-1 , stringBuffer.length()-1 , "") ;
        return stringBuffer.toString();
    }

    private static void addBasicAuthentication(HttpURLConnection urlConnection)
    {
        final String basicAuth = "Basic "+ Base64.encodeToString( (Constants.APP_KEY + ":" + Constants.APP_SECRET ).getBytes(),
                Base64.NO_WRAP);
        urlConnection.setRequestProperty( Constants.AUTHORIZATION,  basicAuth);
    }

    public static boolean hasInternetConnection(Context context)
    {
        ConnectivityManager connectivityManager =
                (( ConnectivityManager ) context.getSystemService(context.CONNECTIVITY_SERVICE));
        return connectivityManager != null &&
                connectivityManager.getActiveNetworkInfo() != null &&
                connectivityManager.getActiveNetworkInfo().isConnected();
    }























}
