# Mascot Studio — Quaternius e Tung Tung Sahur / Unified 0.11.0-alpha.1

Abra **Cosmetic Studio → Pet** e ative **Mascot**. O personagem é o modelo pronto [Alien, de Quaternius](https://poly.pizza/m/HYUUkdugoP), escolhido pelo usuário, com a malha, os materiais e o esqueleto do autor. O botão Player view alterna entre a prévia ampliada e a vista junto ao jogador. **Model** alterna entre Alien e **Tung Tung Sahur**, sem remover nenhum deles. Trocar o modelo aplica a paleta Original e conserva ativação, modo, escala e comportamento.

- **Follow:** acompanha o dono com movimento amortecido; distância, altura e lado são ajustáveis.
- **Shoulder:** fica junto ao ombro, com proporção reduzida. Os sliders de distância e altura ficam desabilitados nesse modo.
- **Orbit:** circula o dono; distância controla o raio e Animation speed altera a velocidade.
- **Original:** restaura as cores base do autor. Alien, Mint e Solar continuam disponíveis, com cores HSV/HEX para pele e faixa. Olhos, reflexos e unhas mantêm seus materiais.
- **Behavior:** animação, reação aos emotes, descanso após seis segundos parado e lado do companheiro.

O movimento aciona **Walk**; ao parar, o modelo retorna a **Idle**. Emotes reais do jogador acionam **Clapping**, e o descanso usa **Sitting**. São animações do arquivo original, com transições graduais. Animation OFF mantém uma pose neutra visível. O modelo não possui uma animação facial nativa de piscar ou uma animação específica de dormir; o descanso é sentado. Os 14 clips originais são preservados nos dados, mas apenas esses quatro estados estão ligados ao comportamento do mascote.

## Tung Tung Sahur

O segundo modelo é [Tung Tung Tung Sahur, por CalinnHotCake](https://poly.pizza/m/Q6SvoDhhwA), com o taco e o rosto do arquivo original. Possui 872 triângulos e seis materiais. A licença é **CC BY 3.0**; autor, fonte e licença aparecem no editor e ficam incluídos no JAR.

O arquivo do Tung é estático, sem esqueleto, animações de braços/pernas ou áudio. O client fornece flutuação, inclinação ao acompanhar movimento e um balanço suave do modelo inteiro durante emotes. Follow, Shoulder e Orbit funcionam com o mesmo seguidor local. Animation OFF mantém a pose original imóvel. [GLB, atribuição, licença e conversão](../third_party/tung-tung-sahur/README.md).

## Perfis e limites

Modelo, cores, modo, tamanho, posição e preferências continuam nos perfis Unified e nos três slots de looks. Perfis anteriores conservam seus valores; perfis sem mascote começam com o recurso desligado. Os overrides locais por UUID também podem ter sua própria aparência; a reação aos emotes usa o playback do jogador local.

É um companheiro visual local, sem criar entidades ou enviar comandos ao servidor. Não possui inventário, combate ou navegação física. Posições e estados transitórios não são salvos. Mudança de mundo/perfil, teleportes, invisibilidade, morte, distância e o master de cosméticos continuam sendo tratados pelo renderer.

O limite é de quatro mascotes, subordinado ao orçamento de cosméticos e com prioridade para o jogador local. Orçamento zero e Mascot OFF limpam a simulação. Malha, pesos e clips são compartilhados; matrizes e buffers de renderização são reutilizados. O modelo fica incluído no JAR, sem baixar arquivos durante o jogo.

Modelo com licença **CC0 1.0**, crédito preservado e [GLB original, licença, hashes e instruções de conversão](../third_party/quaternius-alien/README.md). A iluminação simples do Forge não é o renderizador PBR do site de origem.

O release publicado 0.10 continua disponível. Esta implementação permanece no PR #28, sem merge ou novo release.
