# Auras reativas — Unified 0.9.0-alpha.1

Abra **Cosmetic Studio → Auras** ou use clique direito em **Modules → Player → Aura Studio**. A opção **Classic particles** preserva a aura original. Escolha **Reactive** para os efeitos novos; perfis antigos continuam usando Classic até você mudar essa opção.

## Aparência

**Arcane** combina círculos com runas geométricas originais no chão. **Neon** usa três órbitas inclinadas e cristais. **Celestial** usa halos e cristais flutuantes. Cada estilo permite escolher duas cores por HSV/HEX, raio, opacidade, intensidade e velocidade da animação.

Os presets ativam explicitamente a aura e o renderizador reativo. Aura ON/OFF controla a exibição; desativar Reactive volta à opção Classic. O master de cosméticos e a visibilidade do look continuam sendo respeitados.

## Reações reais

- **Movement:** a velocidade real de deslocamento aumenta a energia e o raio. Parar libera a reação gradualmente.
- **Emotes:** a reprodução real de um emote no Alien aumenta a intensidade e adiciona pulsação.
- **Night:** a aura reage à hora do mundo. Se o World Studio estiver com uma hora visual ativa, essa hora é usada.

Cada reação tem chave e ganho próprios. **Idle energy** define a intensidade em repouso; zero permite que o efeito apareça somente quando houver um gatilho. **Response seconds** controla a suavização da entrada; a saída é mais gradual. O editor mostra o jogador real. **Play selected emote** fecha o editor e inicia o emote escolhido no mundo.

As preferências são salvas nos perfis Unified e nos slots de looks, incluindo exportação/importação. Copiar um look não compartilha o objeto editável da aura. Os estados de movimento/emote/noite não são salvos e são descartados ao trocar mundo ou perfil.

## Desempenho e limites

O efeito usa geometria local, teste de profundidade, até oito estados de jogadores e no máximo 192 unidades de geometria por passe. O orçamento de partículas e a distância do Performance Engine também limitam a renderização; o brilho externo é reduzido no preset Competitive ou quando a qualidade adaptativa cai. Orçamento zero e Aura OFF limpam os estados.

As auras reativas dependem das animações globais e do look. A aura Classic mantém seu comportamento anterior. Não há luz dinâmica no terreno, alteração de vida/atributos, áudio ou sincronização pela rede. Outros jogadores não veem sua aura somente por você instalar o Alien. Overrides de jogadores conhecidos continuam sendo configurações locais; o gatilho Emotes acompanha somente o jogador local.

[Resultados dos testes](REACTIVE-AURAS-VALIDATION.md) · [Capturas atuais](../captures/README.md) · [Vídeo atual](../captures/video-atual/README.md).
