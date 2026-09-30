const URL_SERVLET_USUARIO = "../UsuarioServlet";

function validarFormularioLogin() {
  let valido = true;
  const cpf = document.getElementById("cpf").value;
  const senha = document.getElementById("senha").value;

  ["cpf", "senha"].forEach(limparErro);

  if (!cpfValido(cpf)) {
    marcarErro("cpf", "Informe um CPF válido.");
    valido = false;
  }
  if (senha.length === 0) {
    marcarErro("senha", "Informe sua senha.");
    valido = false;
  }
  return valido;
}

async function autenticar() {
  const params = new URLSearchParams({
    acao: "Login",
    cpf: limparCpf(document.getElementById("cpf").value),
    senha: document.getElementById("senha").value
  });

  const resposta = await fetch(URL_SERVLET_USUARIO, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: params
  });
  return await resposta.json();
}

function inicializar() {
  document.getElementById("cpf").addEventListener("input", (evento) => {
    evento.target.value = formatarCpfEnquantoDigita(evento.target.value);
  });

  document.getElementById("form-login").addEventListener("submit", async (evento) => {
    evento.preventDefault();
    mostrarMensagemGeral("mensagem-geral", "", "");

    if (!validarFormularioLogin()) return;

    try {
      const resultado = await autenticar();
      if (resultado.sucesso) {
       
        sessionStorage.setItem("usuarioId", resultado.id);
        sessionStorage.setItem("usuarioNome", resultado.nome);
        window.location.href = "index.html";
      } else {
        mostrarMensagemGeral("mensagem-geral", resultado.erro, "erro");
      }
    } catch (erro) {
      mostrarMensagemGeral("mensagem-geral", "Erro de conexão com o servidor. Tente novamente.", "erro");
    }
  });
}

document.addEventListener("DOMContentLoaded", inicializar);