# Tipografías de Underc0de RF

Según el **Manual de Marca de Underc0de** (sección 02. Tipografía):

| Uso (jerarquía del manual) | Fuente | Dónde se aplica en la app |
|---|---|---|
| Logo, títulos y encabezados | **Usuzi** Regular (`usuzi.ttf`) | Título de cada pantalla (`ScreenTitle`), "UNDERC0DE RF", título del diálogo SOS |
| Subtítulos, bajadas y frases destacadas | **Univia Pro** Medium/Bold — *opcional*, si no **Roboto** | Bajadas bajo cada título, "[!] AVISO…", "PREPARÁ EL PTT", texto de todos los botones |
| Textos | **Roboto** Regular / Medium / Bold | Cuerpo de texto, pestañas, campos, estados |
| Acento gráfico puntual | **Pixelify Sans** Medium | Línea `SYS://` del encabezado, tarjeta `SSTV.LOG`, número de la cuenta regresiva |

## Univia Pro (opcional)

Los archivos de Univia Pro provienen de **Adobe Fonts** (Typekit). La licencia de Adobe Fonts
no permite empaquetar la fuente dentro de una app distribuida, por eso **no está incluida**.
Si Underc0de consigue una licencia de Univia Pro que permita embeberla en apps (por ejemplo,
comprada a Mostardesign), copiar los archivos así y recompilar, sin tocar código:

- `app/src/main/res/font/univia_pro_medium.ttf`
- `app/src/main/res/font/univia_pro_bold.ttf`

`BrandFonts.init()` las detecta solas; mientras no estén, se usa Roboto (también tipografía secundaria del manual).

## Usuzi

Incluida (`usuzi.ttf`, Regular, la variante que indica el manual). Es de **Iconian Fonts**
(© 2019 www.iconian.com). Iconian pide licencia para uso comercial: si la app se distribuye
públicamente, conviene confirmar que la licencia de Underc0de cubre embeberla en apps.

## Licencias de las fuentes incluidas

- `pixelify_sans_medium.ttf`: Pixelify Sans Medium. Copyright 2021 The Pixelify Sans Project Authors (https://github.com/eifetx/Pixelify-Sans) Licencia: This Font Software is licensed under the SIL Open Font License, Version 1.1. This license is available with a FAQ at: https://scripts.sil.org/OFL (https://scripts.sil.org/OFL)
- `roboto_bold.ttf`: Roboto Bold. Copyright 2011 The Roboto Project Authors (https://github.com/googlefonts/roboto-classic) Licencia: This Font Software is licensed under the SIL Open Font License, Version 1.1. This license is available with a FAQ at: https://openfontlicense.org (https://openfontlicense.org)
- `roboto_medium.ttf`: Roboto Medium. Copyright 2011 The Roboto Project Authors (https://github.com/googlefonts/roboto-classic) Licencia: This Font Software is licensed under the SIL Open Font License, Version 1.1. This license is available with a FAQ at: https://openfontlicense.org (https://openfontlicense.org)
- `roboto_regular.ttf`: Roboto Regular. Copyright 2011 The Roboto Project Authors (https://github.com/googlefonts/roboto-classic) Licencia: This Font Software is licensed under the SIL Open Font License, Version 1.1. This license is available with a FAQ at: https://openfontlicense.org (https://openfontlicense.org)
- `usuzi.ttf`: Usuzi Regular. © 2019 Iconian Fonts - www.iconian.com. Licencia según los términos de Iconian Fonts.
