package org.code.protocol;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AssetUrlGeneratorTest {
  private static final String STARTER_ASSET_FILE = "file1.png";
  private static final String STARTER_ASSET_DATA =
      new JSONObject(
              Map.of(
                  "starter_assets",
                  List.of(
                      Map.of("filename", STARTER_ASSET_FILE, "otherKey", "otherValue"),
                      Map.of("otherKey", "otherValue")),
                  "otherKey",
                  "otherValue"))
          .toString();
  private static final String DASHBOARD_URL = "https://localhost-studio.code.org";
  private static final String CHANNEL_ID = "channelId";
  private static final String LEVEL_ID = "levelId";

  private HttpClient httpClient;
  private HttpResponse<String> httpResponse;
  private AssetUrlGenerator unitUnderTest;

  @BeforeEach
  public void setUp() throws InterruptedException, IOException {
    httpClient = mock(HttpClient.class);
    httpResponse = mock(HttpResponse.class);

    when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(httpResponse);
    when(httpResponse.statusCode()).thenReturn(200);
    when(httpResponse.body()).thenReturn(STARTER_ASSET_DATA);

    unitUnderTest = new AssetUrlGenerator(DASHBOARD_URL, CHANNEL_ID, LEVEL_ID, httpClient);
  }

  @Test
  public void testGenerateAssetUrlThrowsExceptionIfErrorFetchingStarterAssetsList()
      throws InterruptedException, IOException {
    final IOException wrappedException = new IOException();
    when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenThrow(wrappedException);

    final Exception exception =
        assertThrows(
            InternalServerRuntimeError.class, () -> unitUnderTest.generateAssetUrl("filename"));
    assertEquals(exception.getMessage(), InternalErrorKey.INTERNAL_EXCEPTION.toString());
    assertSame(exception.getCause(), wrappedException);
  }

  @Test
  public void testGenerateAssetUrlThrowsExceptionIfInvalidStarterAssetsResponse() {
    final String errorResponseBody = "error";

    when(httpResponse.statusCode()).thenReturn(500);
    when(httpResponse.body()).thenReturn(errorResponseBody);

    final Exception exception =
        assertThrows(
            InternalServerRuntimeError.class, () -> unitUnderTest.generateAssetUrl("filename"));
    assertEquals(exception.getMessage(), InternalErrorKey.INTERNAL_EXCEPTION.toString());
    assertEquals(exception.getCause().getMessage(), errorResponseBody);
  }

  @Test
  public void testGenerateAssetUrlThrowsExceptionIfErrorParsingStarterAssetJson() {
    when(httpResponse.body()).thenReturn("[ /// invalid /// ]");

    final Exception exception =
        assertThrows(
            InternalServerRuntimeError.class, () -> unitUnderTest.generateAssetUrl("filename"));
    assertEquals(exception.getMessage(), InternalErrorKey.INTERNAL_EXCEPTION.toString());
    assertTrue(exception.getCause() instanceof JSONException);
  }

  @Test
  public void testGenerateAssetUrlReturnsStarterAssetUrlForStarterAssetFilename() throws Exception {
    assertTrue(
        unitUnderTest
            .generateAssetUrl(STARTER_ASSET_FILE)
            .contains(
                String.format(
                    "%s/level_starter_assets/%s/%s", DASHBOARD_URL, LEVEL_ID, STARTER_ASSET_FILE)));
  }

  @Test
  public void testGenerateAssetUrlReturnsUserAssetForUserFilename() {
    final String userAssetFile = "userFile.wav";
    assertTrue(
        unitUnderTest
            .generateAssetUrl(userAssetFile)
            .contains(
                String.format("%s/v3/assets/%s/%s", DASHBOARD_URL, CHANNEL_ID, userAssetFile)));
  }

  @Test
  public void testGenerateAssetUrlDoesNotLoadStarterAssetsListIfAlreadyLoaded()
      throws InterruptedException, IOException {
    verify(httpClient, never()).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

    unitUnderTest.generateAssetUrl("file1.wav");
    verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

    unitUnderTest.generateAssetUrl("file2.wav");
    verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
  }
}
