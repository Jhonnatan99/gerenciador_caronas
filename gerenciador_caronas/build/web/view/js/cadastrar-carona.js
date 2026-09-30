const URL_SERVLET = "../CaronaServlet";

function validarFormularioCarona() {
  let valido = true;

  const origem = document.getElementById("origem").value.trim();
  const destino = document.getElementById("destino").value.trim();
  const data = document.getElementById("data").value;
  const horario = document.getElementById("horario").value;
  const vagas = document.getElementById("vagas").value;
  const valor = document.getElementById("valor").value;

  ["origem", "destino", "data", "horario", "vagas", "valor"].forEach(limparErro);

  if (origem.length < 2) {
    marcarErro("origem", "Informe a origem da carona.");
    valido = false;
  }

  if (destino.length < 2) {
    marcarErro("destino", "Informe o destino da carona.");
    valido = false;
  }

  if (origem && destino && origem.toLowerCase() === destino.toLowerCase()) {
    marcarErro("destino", "Origem e destino não podem ser iguais.");
    valido = false;
  }

  if (!data) {
    marcarErro("data", "Informe a data da carona.");
    valido = false;
  } else {
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    const dataEscolhida = new Date(data + "T00:00:00");
    if (dataEscolhida < hoje) {
      marcarErro("data", "A data não pode ser no passado.");
      valido = false;
    }
  }

  if (!horario) {
    marcarErro("horario", "Informe o horário da carona.");
    valido = false;
  }

  const vagasNumero = Number(vagas);
  if (!vagas || !Number.isInteger(vagasNumero) || vagasNumero < 1) {
    marcarErro("vagas", "Informe uma quantidade de vagas válida (mínimo 1).");
    valido = false;
  } else if (vagasNumero > 4) {
    marcarErro("vagas", "O máximo permitido é 4 vagas.");
    valido = false;
  }

  const valorNumero = Number(valor);
  if (!valor || valorNumero <= 0) {
    marcarErro("valor", "Informe um valor por pessoa maior que zero.");
    valido = false;
  }

  return valido;
}

async function cadastrarCaronaNoServidor() {
  const params = new URLSearchParams({
    acao: "CadastrarCarona",
    usuarioId: idUsuarioLogado(),
    origem: document.getElementById("origem").value.trim(),
    destino: document.getElementById("destino").value.trim(),
    data: document.getElementById("data").value,
    horario: document.getElementById("horario").value,
    vagas: document.getElementById("vagas").value,
    valor: document.getElementById("valor").value
  });

  const resposta = await fetchComSessao(URL_SERVLET, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: params
  });
  return await resposta.json();
}

function inicializar() {
  if (!exigirLogin()) return;
  renderizarAreaUsuario();

  document.getElementById("form-carona").addEventListener("submit", async (evento) => {
    evento.preventDefault();
    mostrarMensagemGeral("mensagem-geral", "", "");

    if (!validarFormularioCarona()) return;

    try {
      const resultado = await cadastrarCaronaNoServidor();
      if (resultado.sucesso) {
        mostrarMensagemGeral("mensagem-geral", "Carona publicada com sucesso!", "sucesso");
        document.getElementById("form-carona").reset();
        setTimeout(() => { window.location.href = "index.html"; }, 1200);
      } else {
    
        mostrarMensagemGeral("mensagem-geral", resultado.erro || "Não foi possível cadastrar a carona.", "erro");
      }
    } catch (erro) {
      mostrarMensagemGeral("mensagem-geral", "Erro de conexão com o servidor. Tente novamente.", "erro");
    }
  });
}

document.addEventListener("DOMContentLoaded", inicializar);