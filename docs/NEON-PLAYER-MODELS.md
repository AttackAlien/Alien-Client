# Neon e modelos do jogador / candidato 0.14

O visual toma como referência as asas luminosas, contornos e personalizações apresentados no [site público do LarpClient](https://larpclient.com). A implementação é própria para Forge 1.8.9. Não contém arquivos daquele client.

## Como usar

Entre em um mundo, abra **Cosmetic Studio** e escolha:

- **Glow**: material neon, bloom, cor, intensidade e raio. Os presets **Cyber dragon**, **Crimson dragon** e **Violet halo** aplicam as novas asas articuladas, halo e paletas. As capas, emblemas, asas antigas, chapéus e acessórios continuam disponíveis.
- **Body**: Vanilla, Alien, Tung Tung Sahur ou Kenny. Há tamanho visual, animação, reação aos emotes e um botão para restaurar Vanilla.
- **Parts**: seis peças independentes combináveis — chifres (Classic/Crescent), máscaras (Oni/Tech), ombreiras (Dragon/Knight), caudas (Spade/Crystal), braços (Bracers/Gauntlets) e pernas (Greaves/Boots). Cada slot tem liga/desliga, posição X/Y/Z em pixels do modelo e escala; os valores pertencem ao look e ao perfil Unified.
- **Pet → Behavior**: controles do neon do look também ficam disponíveis no editor do mascote.

Os controles fazem parte do look e dos perfis Unified. O material neon já vem habilitado nos looks; o master de cosméticos mantém a preferência existente. O modelo do jogador continua **Vanilla** até você escolher outro.

## O que está implementado

O material preserva as cores, ilumina os detalhes e cria uma máscara emissiva com teste de profundidade. Um bloom em resolução reduzida espalha a luz em volta da silhueta. O mundo mantém sua iluminação. Modelos atrás de paredes não produzem a máscara visível. O processamento é compartilhado por passagem, com limite de modelos; o preset Competitive mantém a superfície emissiva e dispensa o bloom.

As asas de dragão têm braços e pontas articuladas, membranas curvas, nervuras e contornos luminosos. O halo é uma malha arredondada. A capa usa a textura real, incluindo seu emblema.

A troca do corpo é uma aparência local em terceira pessoa e nos editores. Preserva a entidade, a colisão, o inventário e as funções da armadura. O corpo personalizado mostra seu próprio traje, e a mão em primeira pessoa continua sendo a do Minecraft. Capas, asas, chapéus e halo usam pontos de encaixe ajustados; as novas peças prendem na cabeça, torso e articulações de braços/pernas e acompanham as poses, com movimento suave na cauda. Auras, trilhas e mascotes continuam ligados ao jogador.

- **Alien**: modelo de Quaternius, CC0, com os 14 clips originais. Movimento usa os clips do modelo; emotes ativam sua reação de dança.
- **Sahur**: modelo de CalinnHotCake, CC BY 3.0, com créditos e fonte preservados. O arquivo não tem esqueleto nem animações: a reação usa movimento do corpo inteiro.
- **Kenny**: adaptação original de um personagem de parka, com superfícies arredondadas, capuz, olhos, luvas e segmentos articulados. Usa as poses procedurais existentes de Floss, Orange Justice e outros emotes. Não é um modelo extraído do LarpClient ou de outro jogo.

Na falta de shaders/framebuffer, o caminho alternativo mantém a superfície iluminada e dispensa o bloom. O efeito não cria iluminação dinâmica nos blocos. Ainda não há sincronização dos modelos com outros jogadores, substituição da mão em primeira pessoa ou teste em multiplayer externo.

## Validação

`runNeonSmokeClient` executa o Minecraft real e compara os pixels com material desligado, superfície emissiva e bloom. Também verifica dia/noite, profundidade de uma parede com blocos reais, controles, perfis, snapshots de looks, os três corpos personalizados e os seis acessórios; o smoke passou 110 verificações. A suíte JUnit passou 315 testes, sem falhas. A figura identificada no teste de parede é um ator local de ensaio; não representa um jogador conectado a um servidor externo.

O vídeo inclui os recursos individualmente e mede frames por capítulo. A taxa codificada do MP4 não substitui a medição de renderização. Capturas e recibos finais ficam na galeria e na validação do candidato após os testes completos.
