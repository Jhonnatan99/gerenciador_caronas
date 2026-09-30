const URL_SERVLET_USUARIO = "../UsuarioServlet";

function validarFormularioCadastro() {
  let valido = true;

  const nome = document.getElementById("nome").value.trim();
  const cpf = document.getElementById("cpf").value;
  const email = document.getElementById("email").value.trim();
  const telefone = document.getElementById("telefone").value;
  const senha = document.getElementById("senha").value;
  const confirmarSenha = document.getElementById("confirmarSenha").value;

  ["nome", "cpf", "email", "telefone", "senha", "confirmarSenha"].forEach(limparErro);

  if (nome.length < 3) { marcarErro("nome", "Informe seu nome completo."); valido = false; }
  if (!cpfValido(cpf)) { marcarErro("cpf", "CPF inválido. Confira os números digitados."); valido = false; }
  if (!emailValido(email)) { marcarErro("email", "Informe um e-mail válido."); valido = false; }
  if (!telefoneValido(telefone)) { marcarErro("telefone", "Informe um telefone válido, com DDD."); valido = false; }
  if (!senhaValida(senha)) { marcarErro("senha", "A senha precisa ter pelo menos 6 caracteres."); valido = false; }
  if (confirmarSenha !== senha) { marcarErro("confirmarSenha", "As senhas não coincidem."); valido = false; }

  return valido;
}

async function cadastrarUsuario() {
  const params = new URLSearchParams({
    acao: "CadastrarUsuario",
    nome: document.getElementById("nome").value.trim(),
    cpf: limparCpf(document.getElementById("cpf").value),
    email: document.getElementById("email").value.trim(),
    telefone: document.getElementById("telefone").value,
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

  document.getElementById("form-cadastro").addEventListener("submit", async (evento) => {
    evento.preventDefault();
    mostrarMensagemGeral("mensagem-geral", "", "");

    if (!validarFormularioCadastro()) return;

    try {
      const resultado = await cadastrarUsuario();
      if (resultado.sucesso) {
        mostrarMensagemGeral("mensagem-geral", "Conta criada com sucesso! Redirecionando para o login...", "sucesso");
        setTimeout(() => { window.location.href = "login.html"; }, 1500);
      } else {
        mostrarMensagemGeral("mensagem-geral", resultado.erro || "Não foi possível criar a conta.", "erro");
      }
    } catch (erro) {
      mostrarMensagemGeral("mensagem-geral", "Erro de conexão com o servidor. Tente novamente.", "erro");
    }
  });
}

document.addEventListener("DOMContentLoaded", inicializar);