package com.bosonshiggs.ComponentsTranslate;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.common.ComponentCategory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.util.Locale;

import org.json.JSONObject;

import android.util.Log;

@DesignerComponent(version = 1,
    description = "Tradutor que adapta textos de componentes para o idioma do dispositivo usando chamadas HTTP",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png")

@SimpleObject(external = true)
public class ComponentsTranslate extends AndroidNonvisibleComponent {

    private String targetLanguage; // Variável de instância para o idioma de destino
    
    public ComponentsTranslate(ComponentContainer container) {
        super(container.$form());
    }

    @SimpleFunction(description = "Replaces component texts with translation in the device language")
    public void ReplaceTextsWithTranslationOnDeviceLanguage() {
        translateFormComponents(form);
    }

    private void translateFormComponents(Form form) {
        for (Component component : form.getChildren()) {
            if (component instanceof AndroidViewComponent) {
                translateComponentsRecursive((AndroidViewComponent) component);
            }

            translateComponentIfTextHolder(component);
        }
    }

    private void translateComponentsRecursive(AndroidViewComponent container) {
        // Verifica se o container é um tipo de contêiner que pode conter outros componentes
        if (container instanceof ComponentContainer) {
            ComponentContainer compContainer = (ComponentContainer) container;

            // Itera sobre os componentes filho do contêiner
            for (Component component : compContainer.getChildren()) {
                // Se o componente filho for também um contêiner, chama a função recursivamente
                if (component instanceof AndroidViewComponent) {
                    translateComponentsRecursive((AndroidViewComponent) component);
                }

                // Traduz o texto do componente, se aplicável
                translateComponentIfTextHolder(component);
            }
        }
    }

    private void translateComponentIfTextHolder(Component component) {
    	try {
    		String textoOriginal = getTextoComponente(component);
            String textoTraduzido = TraduzirTexto(textoOriginal, this.targetLanguage);
            setTextoComponente(component, textoTraduzido);
            Log.i("ComponentsTranslate", "Tradução bem-sucedida para: " + component.getClass().getSimpleName());
            OnTranslationSuccess(component.getClass().getSimpleName(), textoOriginal, textoTraduzido);
        } catch (Exception e) {
            Log.e("ComponentsTranslate", "Erro na tradução: " + e.getMessage());
            OnTranslationError(e.getMessage());
        }
    }

    // Métodos getTextoComponente e setTextoComponente...
    private String getTextoComponente(Component component) {
        if (component instanceof TextBox) {
            return ((TextBox) component).Text();
        } else if (component instanceof Label) {
            return ((Label) component).Text();
        } else if (component instanceof Button) {
            return ((Button) component).Text();
        }
        // Adicione condições para outros componentes que contenham texto, se necessário
        return "";
    }

    private void setTextoComponente(Component component, String texto) {
        if (component instanceof TextBox) {
            ((TextBox) component).Text(texto);
        } else if (component instanceof Label) {
            ((Label) component).Text(texto);
        } else if (component instanceof Button) {
            ((Button) component).Text(texto);
        }
        // Adicione condições para outros componentes que contenham texto, se necessário
    }

    
    private String TraduzirTexto(String textoOriginal, String idiomaAlvo) {
        try {
            String apiURL = "https://libretranslate.com/translate";
            String data = "q=" + URLEncoder.encode(textoOriginal, "UTF-8") +
                          "&source=" + "auto" +
                          "&target=" + idiomaAlvo +
                          "&format=" + "text";
            
            URL url = new URL(apiURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try(OutputStream outputStream = conn.getOutputStream()) {
                outputStream.write(data.getBytes("UTF-8"));
            }

            StringBuilder response = new StringBuilder();
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            // Análise da resposta JSON
            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.getString("translatedText");

        } catch (Exception e) {
            e.printStackTrace();
            return "Erro na tradução";
        }
    }
       
    @SimpleEvent(description = "Event fired when translation is successful.")
    public void OnTranslationSuccess(String componentName, String originalText, String translatedText) {
        EventDispatcher.dispatchEvent(this, "OnTranslationSuccess", componentName, originalText, translatedText);
    }

    @SimpleEvent(description = "Event triggered in case of translation error")
    public void OnTranslationError(String errorMessage) {
        EventDispatcher.dispatchEvent(this, "OnTranslationError", errorMessage);
    }
}
