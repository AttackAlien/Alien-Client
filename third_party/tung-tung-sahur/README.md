# Tung Tung Tung Sahur — CalinnHotCake

**Autor:** CalinnHotCake. **Licença:** Creative Commons Attribution 3.0 Unported
(CC BY 3.0). Modelo: https://poly.pizza/m/Q6SvoDhhwA . Licença completa:
https://creativecommons.org/licenses/by/3.0/ .

GLB original: `Tung-Tung-Sahur.glb`, SHA-256
`ee51070ee71acf12f88ddd551a70411f1f390447baf8568d352d39b7da464575`.

O modelo pronto contém 1.792 vértices, 872 triângulos e seis materiais, incluindo
o corpo, rosto, olhos e taco. Não possui esqueleto ou animações originais.

Alterações no Alien Client: conversão de coordenadas, rotação de orientação,
normalização de escala/centro, iluminação compatível com Forge e paletas
opcionais. A geometria é a do GLB original, preservado aqui. O recurso convertido
é incluído no JAR e não baixa arquivos durante o jogo.

O client anima a flutuação, inclinação e uma reação suave do modelo inteiro.
Não são animações de braços/pernas do autor. Follow, Shoulder e Orbit usam o
mesmo acompanhamento real do dono. Não há voz/áudio ou ataques do personagem.

O crédito ao autor, a fonte e a licença aparecem no editor, nos recursos
META-INF/licenses do JAR e nesta documentação. A CC BY 3.0 do modelo permanece
válida; o modelo não é anunciado como criação original do Alien Client.

Conversão com Python 3 e NumPy:

```bash
python3 scripts/import-companion-model.py --rigid --yaw -90 third_party/tung-tung-sahur/Tung-Tung-Sahur.glb src/main/resources/assets/alienclient/models/companion/tung.rigid.gz
```
