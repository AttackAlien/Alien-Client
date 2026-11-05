# Fantasma Neon — efeito de eliminação

O Unified 0.13 adiciona um espírito 3D original, arredondado e translúcido. Ele nasce no local da eliminação, se desprende devagar, sobe com aceleração suave, ondula o tecido e os braços, pisca e se dissolve de baixo para cima em pequenos brilhos. O movimento usa tempo decorrido, sem depender da quantidade de frames.

## Ativar e personalizar

Abra **Effects Studio → Ghost Elimination**. Na Module Gallery, também aparece em **Render → Ghost Elimination**. O efeito começa desligado.

- Cores **Neon**, **Spectral** e **Rose**, ou escolha livre no seletor de cores.
- Duração de 1,5 a 4,5 segundos; altura de subida, tamanho, opacidade e brilho.
- Partículas e rastros suaves independentes; **Calm motion** reduz oscilações e remove ecos.
- Filtros de jogadores e mobs, distância de 8 a 48 blocos e limite de um a seis fantasmas, respeitando o orçamento de efeitos do perfil de desempenho.
- Preferências salvas nos perfis Unified. Os demais efeitos, cosméticos e o HUD de música continuam independentes.

A prévia 3D usa a mesma geometria e animação do mundo. **Replay preview** reinicia a apresentação; a prévia é identificada e nunca conta como uma eliminação.

## Quando aparece

Em um mundo integrado, o client recebe o evento real do servidor local e verifica o autor creditado pela mecânica vanilla, o alvo e a morte recebida pelo client. Isso permite efeitos em eliminações de mobs e de outros jogadores presentes nesse mundo.

Em multiplayer, são aceitas mensagens de morte **vanilla traduzíveis**, com o jogador local identificado como autor e o alvo conhecido, seguidas de morte/remoção real do alvo. Um ataque, clique, desconexão, texto comum do chat ou morte ambiental sem autoria não basta.

**Feeds personalizados de servidores que enviam apenas texto não são interpretados nesta versão.** Se o servidor oculta as mensagens vanilla ou usa outro formato, o efeito pode não disparar. Não houve validação em um servidor multiplayer externo.

O fantasma é uma forma estilizada original, com rosto e tecido próprios. Não é uma cópia da skin da vítima. É um efeito visual local: não cria entidades no mundo, altera dano ou envia ações de combate.

## Renderização e limites

Há buffers de vértices reutilizados, estado transitório limitado e deduplicação de mortes. O efeito respeita profundidade e obstáculos; não foi criado para destacar alvos vivos ou atravessar paredes. Trocar de mundo/perfil ou desligá-lo limpa os efeitos ativos e pendentes.

As capturas reais e o vídeo atual estão na [galeria](../captures/README.md). [Resultados, comandos e limites](GHOST-ELIMINATION-VALIDATION.md).
