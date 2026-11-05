# Alien — Quaternius

Modelo pronto escolhido pelo usuário: **Alien**, por **Quaternius**.

- Modelo e licença: https://poly.pizza/m/HYUUkdugoP
- Autor: https://quaternius.com/
- Licença: **CC0 1.0 Universal** — https://creativecommons.org/publicdomain/zero/1.0/
- SHA-256 do GLB original: `f55d091cd3914977724f4a7887e1866e73c2f2045df391e83c787b6736552ddb`.

`Alien.glb` é o arquivo original, sem reconstrução geométrica. Contém 7.642
vértices, 3.774 triângulos, cinco materiais, 45 juntas e 14 animações do autor.
O arquivo original é preservado aqui para auditoria e novas conversões.

`scripts/import-companion-model.py` converte a malha, pesos e animações para um
recurso compacto, incluído no JAR. A conversão normaliza escala/centro e amostra
as matrizes do esqueleto a 24 Hz, com interpolação durante a renderização. Não
recria o personagem com primitivas. Não baixa modelos durante o jogo.

As cores dos materiais Main/Stripe podem ser personalizadas pelo cliente; Eyes,
White e Nails preservam os materiais do autor. O preset Original restaura as
cores base convertidas de linear para sRGB. O renderer usa iluminação simples
compatível com Forge 1.8.9, sem prometer o mesmo renderizador PBR do site.

Todos os clips são preservados no recurso. Idle, Walk, Clapping e Sitting são
ligados, respectivamente, a repouso normal, movimento, reação aos emotes e
descanso após inatividade. Os demais clips são dados importados disponíveis;
não são ações automáticas de combate ou funcionalidades anunciadas do pet.

Para reproduzir a conversão, com Python 3 e NumPy instalados:

```bash
python3 scripts/import-companion-model.py third_party/quaternius-alien/Alien.glb src/main/resources/assets/alienclient/models/companion/alien.anm.gz
```

Crédito ao Quaternius mantido voluntariamente, além das permissões da CC0.
