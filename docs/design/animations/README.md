# Animaciones de marca — prototipos

Prototipos HTML/CSS de las animaciones propuestas para el splash y los indicadores
de carga. Se abren en cualquier navegador; son autocontenidos (el SVG del logo está
embebido, sin dependencias externas).

Los vectores se derivaron de `app/src/main/res/drawable/ic_launcher.xml`, partiendo
el icono en cuatro capas: círculo de fondo, balón, flecha verde y flecha roja.

| Fichero | Contenido |
|---|---|
| `splash-prototype-v1-el-cambio.html` | Primera propuesta: pop del círculo, balón con overshoot, flechas dibujándose (trim path por máscara de arco) y cola de carga en bucle. Vista en light y dark. |
| `splash-prototype-v2-balon-chutado.html` | Propuesta elegida. El balón gira a alta velocidad y decelera por fricción. Dos variantes: **A** gira en el sitio (recomendada), **B** entra rodando desde fuera. Controles para número de vueltas, cola de carga y cámara lenta. |

## Notas técnicas de los prototipos

- **Deceleración del balón**: 48 keyframes generados con decaimiento exponencial de
  la velocidad angular, `ω(t) = ω₀·e^(−t/τ)` con τ = 0.26. Un `ease-out` Bézier da un
  frenado simétrico y blando que no lee como fricción.
- **Vueltas enteras**: el giro total debe ser múltiplo de 360° para que el frame final
  coincida exactamente con el logo estático.
- **Barrido de las flechas**: máscara SVG con un arco trazado y `stroke-dashoffset`
  animado, equivalente al trim path de Lottie. En Compose el equivalente es un
  `clipPath` con una cuña radial de ángulo animado.
- **Centro de rotación**: (297.64, 298.92) en el espacio de coordenadas del vector
  original. Los prototipos ajustan el `viewBox` a `-160 -160 915.82 919.76` para
  absorber el `translate(104,104) scale(0.65)` del grupo del drawable y poder usar
  ese centro directamente como `transform-origin`.
