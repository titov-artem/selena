package ru.selena.net.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.selena.net.TransportService;
import ru.selena.net.exception.ConflictException;
import ru.selena.net.exception.NoDataException;
import ru.selena.net.model.Host;
import ru.selena.util.collections.ArrayUtils;

import java.io.IOException;

/**
 * Date: 12/18/12
 * Time: 10:41 PM
 *
 * @author Artem Titov
 */
public final class HttpTransportService implements TransportService {
    private static final Logger log = LoggerFactory.getLogger(HttpTransportService.class);

    private static final int TIMEOUT = 30000;

    private HttpClient createHttpClient() {
        final HttpParams params = new BasicHttpParams();
        HttpClientParams.setRedirecting(params, false);
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT);
        HttpConnectionParams.setTcpNoDelay(params, false);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        final ClientConnectionManager connectionManager = new PoolingClientConnectionManager();
        return new DefaultHttpClient(connectionManager, params);
    }

    @Override
    public byte[] get(final byte[] key, final Host host) throws IOException {
        Validate.notNull(key, "Key can't be null");
        Validate.notNull(host, "Host can't be null");

        final HttpGet request = buildGetRequest(key);
        final HttpClient client = createHttpClient();
        final HttpResponse response = client.execute(new HttpHost(host.getHost(), host.getPort()), request);
        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            final String reasonPhrase = response.getStatusLine().getReasonPhrase();
            log.warn("Error response: " + statusCode + "(" + reasonPhrase + ")");
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                throw new NoDataException(reasonPhrase);
            } else {
                throw new IOException(reasonPhrase);
            }
        }
        return IOUtils.toByteArray(response.getEntity().getContent());
    }

    @Override
    public void send(final byte[] dataObject, final Host host) throws IOException {
        Validate.notNull(dataObject, "Data object can't be null");
        Validate.notNull(host, "Host can't be null");

        final HttpPost request = buildSendRequest(dataObject);
        final HttpClient client = createHttpClient();
        final HttpResponse response = client.execute(new HttpHost(host.getHost(), host.getPort()), request);
        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            final String reasonPhrase = response.getStatusLine().getReasonPhrase();
            log.warn("Error response: " + statusCode + "(" + reasonPhrase + ")");
            if (statusCode == HttpStatus.SC_CONFLICT) {
                throw new ConflictException(reasonPhrase);
            } else {
                throw new IOException(reasonPhrase);
            }
        }
    }

    private HttpGet buildGetRequest(final byte[] key) {
        return new HttpGet("/internal/get?key=" + ArrayUtils.toHexString(key));
    }

    private HttpPost buildSendRequest(final byte[] data) {
        final HttpPost request = new HttpPost("/internal/put");
        request.setEntity(new ByteArrayEntity(data, ContentType.APPLICATION_OCTET_STREAM));
        return request;
    }
}
