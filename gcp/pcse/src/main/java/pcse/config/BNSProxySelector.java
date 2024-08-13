package pcse.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BNSProxySelector extends ProxySelector {
  static final String BNS_GATEWAY_HOST = "webproxy.bns";
  static final int BNS_GATEWAY_PORT = 8080;
  static final SocketAddress DEFAULT_PROXY_ADDR =
      new InetSocketAddress(BNS_GATEWAY_HOST, BNS_GATEWAY_PORT);
  static final Proxy DEFAULT_PROXY = new Proxy(Type.HTTP, DEFAULT_PROXY_ADDR);
  static final Set<String> SUPPORTED_PROTOCOL = ImmutableSet.of("http", "https");
  static final Pattern NO_PROXY_HOST =
      Pattern.compile("^(sqladmin.googleapis.com|oauth2.googleapis.com"
          + "|iamcredentials.googleapis.com|storage.googleapis.com"
          + "|cloudkms.googleapis.com|cloudresourcemanager.googleapis.com"
//          + "|secretmanager.googleapis.com"
          + "|127\\.0\\.0\\.1"
          + "|.*.bns"
          + "|localhost"
          + "|.*\\.io"
          + "|.*\\.app)$");
  private static final Logger LOG = LoggerFactory.getLogger(BNSProxySelector.class);
  private final ProxySelector defaultSelector;
  private Optional<Proxy> currentProxy;

  public BNSProxySelector(ProxySelector defaultSelector) {
    this.defaultSelector = Preconditions.checkNotNull(defaultSelector);
    this.currentProxy = Optional.of(DEFAULT_PROXY);
  }

  public static synchronized void applyBNSProxySettings() {
    ProxySelector currentProxySelector = ProxySelector.getDefault();
    if (currentProxySelector instanceof BNSProxySelector) {
      LOG.debug("BNS Proxy settings already applied");
    } else {
      ProxySelector.setDefault(new BNSProxySelector(currentProxySelector));
      LOG.info("Applying the BNS Proxy settings");
    }
  }

  @Override
  public List<Proxy> select(URI uri) {
    if (uri == null) {
      throw new IllegalArgumentException("URI can't be null.");
    }
    return currentProxy
        .filter(proxy -> SUPPORTED_PROTOCOL.contains(uri.getScheme()))
        .filter(proxy -> !NO_PROXY_HOST.matcher(uri.getHost()).matches())
        .map(proxy -> Arrays.asList(proxy))
        .orElse(defaultSelector.select(uri));
  }

  @Override
  public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
    if (uri == null || sa == null || ioe == null) {
      throw new IllegalArgumentException("Argument can't be null");
    }
    if (sa.equals(DEFAULT_PROXY_ADDR)) {
      LOG.warn("BNS Proxy failed to work for {}. Disable BNS Proxy", uri);
      currentProxy = Optional.empty();
    } else {
      defaultSelector.connectFailed(uri, sa, ioe);
    }
  }
}
