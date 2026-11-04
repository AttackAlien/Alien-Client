# Validação — auras reativas e vídeo

Branch `feature/reactive-auras-20261104`, baseada no PR #23. Sem merge na main. [Guia das auras](REACTIVE-AURAS.md).

Rodada local: **216 JUnit** e **307 checks gráficos** na sessão completa do Forge 1.8.9, incluindo todos os testes anteriores e as auras. O teste direcionado de auras passou 65 checks. As capturas finais, o vídeo contínuo e os recibos dos CIs serão registrados nesta página ao concluir a validação.

Os testes de auras exercitam os estilos, movimento nativo, emote real, meia-noite do servidor integrado, chaves de reação, HEX, perfis/looks, rolagem em layout compacto, limites de orçamento, Classic e OFF.

O vídeo usa um fixture separado em mundo local descartável, com capítulos de customizações e demonstração dos módulos Full existentes. Ele será uma gravação contínua do X11, com medição dos frames renderizados; não usa uma sequência de fotos nem interpolação. O fixture não faz parte do JAR distribuído.
