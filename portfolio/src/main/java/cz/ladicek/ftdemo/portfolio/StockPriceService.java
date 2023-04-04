package cz.ladicek.ftdemo.portfolio;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Singleton
public class StockPriceService {
  @Inject @RestClient StockPriceClient client;

  @Inject StatsResource stats;

  private final ConcurrentMap<String, StockPrice> cache = new ConcurrentHashMap<>();

  //    @Fallback(fallbackMethod = "getPriceFallback")
  //    @Timeout
  //    @CircuitBreaker(skipOn = BulkheadException.class)
  //    @Bulkhead
  //    @Retry
  public StockPrice getPrice(String ticker) {
    StockPrice result = client.get(ticker);
    cache.put(ticker, result);
    stats.recordNormal();
    stats.setCacheSize(cache.size());
    return result;
  }

  private StockPrice getPriceFallback(String ticker) {
    stats.recordCached();
    return cache.getOrDefault(ticker, new StockPrice(ticker, null));
  }
}
