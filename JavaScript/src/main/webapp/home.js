{
	let foldersList, alertContainer = document.getElementById('id_alert'), pageOrchestrator = new PageOrchestrator(); // main controller
	window.addEventListener("load", () => {
		if (sessionStorage.getItem("username") == null) {
			window.location.href = "index.html";
		} else {
			pageOrchestrator.start(); // initialize the components
			pageOrchestrator.refresh();
		} // display initial content
	}, false);

	function PageOrchestrator() {
		this.start = function() {
			foldersList = new FoldersList(alertContainer, document.getElementById("id_listcontainer"), document.getElementById("id_listcontainerbody"));
			localStorage.setItem('folderId', null);
			localStorage.setItem('folderName', null);
		};

		this.refresh = function(message) {
			if (message === "modalWindow") {
				alertContainer.textContent = "";
				document.getElementById("id_createfolderform").style.pointerEvents = "none";
			} else if (message === "document") {
				document.getElementById("id_document").style.display = "block";
				document.getElementById("id_home").style.display = "none";
				document.getElementById("id_content").style.display = "none";
				showDocument;
			} else if (message === "content") {
				foldersList.show();
				document.getElementById("id_home").style.display = "none";
				document.getElementById("id_content").style.display = "block";
				document.getElementById("id_document").style.display = "none";

			} else {
				foldersList.show();
				document.getElementById("id_home").style.display = "block";
				document.getElementById("id_content").style.display = "none";
				document.getElementById("id_bin").style.display = "block";
			}
		};
	}

	function FoldersList(_alert, _listcontainer, _listcontainerbody) {
		this.alert = _alert;
		this.listcontainer = _listcontainer;
		this.listcontainerbody = _listcontainerbody;

		this.reset = function() {
			this.listcontainer.style.visibility = "hidden";
		}

		this.show = function() {
			let self = this;
			makeCall("GET", "getFolders", null,
				function(req) {
					if (req.readyState === 4) {
						let message = req.responseText;
						if (req.status === 200) {
							let foldersToShow = JSON.parse(req.responseText);
							if (foldersToShow.length === 0) {
								document.getElementById("id_bin").style.display = "none";
								self.update(foldersToShow);
								return;
							}
							self.update(foldersToShow);

						} else if (req.status === 403) {
							window.location.href = req.getResponseHeader("Location");
							window.sessionStorage.removeItem('username');
						}
						else {
							self.alert.textContent = "ERROR: " + message;
						}
					}
				}
			);
		};

		this.update = function(arrayFolders) {
			this.alert = _alert;
			this.listcontainer = _listcontainer;
			this.listcontainerbody = _listcontainerbody;

			this.reset = function() {
				this.listcontainer.style.visibility = "hidden";
			}
			let self = this;
			this.listcontainerbody.innerHTML = ""; // Svuota il contenuto precedente
			arrayFolders.forEach(function(folder) {
				let row = document.createElement("li");
				let cell = document.createElement("span");
				row.name = folder.name;
				row.isFolder = true;
				id = folder.folderId;
				cell.id = id;
				row.id = id;
				cell.isFolder = true;
				row.ondrop = dropFolder;
				row.ondragover = allowDrop;
				row.draggable = true;
				row.ondragstart = drag;
				cell.innerHTML = `<a href="#" draggable= "false" onclick="showSubfolder('${id}', '${folder.name}')">${folder.name}</a>`;

				let indentationLevel = folder.path.split('/').length;
				row.classList.add('indent-' + indentationLevel);
				row.appendChild(cell);

				// Contenitore per i bottoni con padding sinistro
				let buttonContainer = document.createElement("href");
				buttonContainer.style.paddingLeft = "100px";

				// Aggiungi il pulsante "Aggiungi sottocartella"
				let addSubfolderBtn = document.createElement("button");
				addSubfolderBtn.classList.add("btn");
				addSubfolderBtn.textContent = "Aggiungi sottocartella";
				buttonContainer.appendChild(addSubfolderBtn);
				addSubfolderBtn.addEventListener("click", function(e) { //funzione per aggiungere sottocartella
					addSubfolder(e);
				});


				let space1 = document.createElement("td");
				buttonContainer.appendChild(space1);

				// Aggiungi il pulsante "Aggiungi documento"
				let addDocumentBtn = document.createElement("button");
				addDocumentBtn.textContent = "Aggiungi documento";
				addDocumentBtn.classList.add("btn");
				buttonContainer.appendChild(addDocumentBtn);
				addDocumentBtn.addEventListener("click", function(e) { //funzione per aggiungere documento
					addDocument(e);
				});
				row.appendChild(buttonContainer); // Aggiunge il contenitore dei bottoni alla riga

				let space = document.createElement("td");
				row.appendChild(space);

				self.listcontainerbody.appendChild(row); //aggiungo row al body
			});
			//aggiungo il form in fondo per creare una cartella primaria
			row = document.createElement("tr");
			cell = document.createElement("span");
			row.appendChild(cell);
			let text = document.createElement("label");
			text.textContent = "Form per creare una cartella primaria: "
			row.appendChild(text);
			let name = document.createElement("input");
			name.name = "name";
			name.placeholder = "Nome";
			row.appendChild(name);
			let addfolderBtn = document.createElement("button");
			addfolderBtn.classList.add("btn");
			addfolderBtn.textContent = "Crea";
			row.id = "id_primary";
			row.appendChild(addfolderBtn);
			addfolderBtn.addEventListener("click", function() {
				let foldername = name.value.trim();
				makeCall2("POST", `createFolder?folderId=-1&name=${encodeURIComponent(foldername)}`
					, null, function(req) {
						if (req.readyState === XMLHttpRequest.DONE) {
							let message = req.responseText;
							if (req.status === 200) {
								// Refresh the folder list after successful creation
								pageOrchestrator.refresh("home");
							} else if (req.status === 403) {
								window.location.href = req.getResponseHeader("Location");
								window.sessionStorage.removeItem('username');
							} else {
								self.alert.textContent = "ERROR: " + message;
								setTimeout(function() {
									self.alert.textContent = "";
								}, 3000);
							}
						}
					});
			});

			self.listcontainerbody.appendChild(row);

			this.listcontainer.style.visibility = "visible";
		};
	}


	function showSubfolder(id, name) {
		this.listcontainer = document.getElementById("id_content");
		this.listcontainerbody = document.getElementById("id_contentbody");

		localStorage.setItem('folderName', name);
		localStorage.setItem('folderId', id);

		let folderId = id;
		if (folderId !== null && folderId !== undefined && folderId !== "") {
			makeCall2("GET", `getContent?folderId=${encodeURIComponent(folderId)}`, null, function(req) {
				if (req.readyState === 4) {
					let message = req.responseText;
					if (req.status === 200) {
						let jsonResponse = JSON.parse(req.responseText);
						let foldersToShow = jsonResponse.folders;
						let documentsToShow = jsonResponse.documents;
						this.update(foldersToShow, documentsToShow); // self visible by closure
						pageOrchestrator.refresh("content");
					} else if (req.status === 403) {
						window.location.href = req.getResponseHeader("Location");
						window.sessionStorage.removeItem('username');
					}
					else {
						alertContainer.textContent = "ERROR: " + message;
					}
				}
			});
		}
		this.update = function(arrayFolders, arrayDocuments) {
			this.listcontainer = document.getElementById("id_content");
			this.listcontainerbody = document.getElementById("id_contentbody");

			let self = this;
			this.listcontainerbody.innerHTML = ""; // Svuota il contenuto precedente

			let header = document.createElement("h1");
			header.innerText = "Contenuto cartella " + name;
			self.listcontainerbody.appendChild(header);

			header = document.createElement("h2");
			header.innerText = "Le tue cartelle:";
			self.listcontainerbody.appendChild(header);
			arrayFolders.forEach(function(folder) {
				if (folder == null || folder.name == null || folder.path == null || folder.date == null) {
					alertContainer.textContent = "folder currupted"
					return;
				}
				let row = document.createElement("li");
				let cell = document.createElement("span");
				cell.id = folder.folderId;
				row.id = folder.folderId;
				row.fid = id;
				cell.innerText = folder.name;
				row.appendChild(cell);

				let space = document.createElement("td");
				row.appendChild(space);

				self.listcontainerbody.appendChild(row); //aggiungo row al body
			});

			header = document.createElement("h2");
			header.innerText = "I tuoi documenti:";
			self.listcontainerbody.appendChild(header);
			arrayDocuments.forEach(function(doc) {
				if (doc == null || doc.name == null || doc.summary == null || doc.type == null || doc.path == null || doc.date == null) {
					alertContainer.textContent = "file currupted"
					return;
				}
				let row = document.createElement("li");
				let cell = document.createElement("span");
				row.fid = id;
				cell.id = doc.documentId;
				row.id = doc.documentId;
				cell.isFolder = false;
				cell.draggable = true;
				cell.ondragstart = drag;
				cell.innerText = doc.name;
				row.appendChild(cell);

				// Contenitore per i bottoni con padding sinistro
				let buttonContainer = document.createElement("href");
				buttonContainer.style.paddingLeft = "100px";

				let showDocBtn = document.createElement("button");
				showDocBtn.classList.add("btn");
				showDocBtn.textContent = "Accedi";
				showDocBtn.addEventListener("click", function(e) {
					showDocument(e);
				});
				buttonContainer.appendChild(showDocBtn);
				row.appendChild(buttonContainer); // Aggiunge il contenitore dei bottoni alla riga

				let space = document.createElement("td");
				row.appendChild(space);

				self.listcontainerbody.appendChild(row); //aggiungo row al body
			});
			let prevBtn = document.createElement("button");
			prevBtn.classList.add("btn");
			prevBtn.textContent = "Pagina precedente";
			prevBtn.addEventListener("click", function() {
				pageOrchestrator.refresh("home");
			});
			self.listcontainerbody.appendChild(prevBtn);
			this.listcontainer.style.visibility = "visible";
		};
	}

	function showDocument(e) {
		let self = this;

		this.alert = alertContainer;
		this.listcontainer = document.getElementById("id_document");
		this.listcontainerbody = document.getElementById("id_documentbody");

		let parentCell = e.target.closest("li");
		let documentId = parentCell.id;
		if (documentId !== null && documentId !== undefined && documentId !== "") {
			makeCall2("GET", `getDocument?documentId=${encodeURIComponent(documentId)}`, null, function(req) {
				if (req.readyState === 4) {
					let message = req.responseText;
					if (req.status === 200) {
						let documentToShow = JSON.parse(req.responseText);
						if (documentToShow == null || documentToShow.name == null || documentToShow.summary == null || documentToShow.type == null || documentToShow.path == null || documentToShow.date == null) {
							alertContainer.textContent = "file currupted"
						}
						else {
							self.updatedocument(documentToShow); // Utilizza il riferimento al contesto esterno
							pageOrchestrator.refresh("document");
						}
					} else if (req.status === 403) {
						window.location.href = req.getResponseHeader("Location");
						window.sessionStorage.removeItem('username');
					} else {
						alertContainer.textContent = "ERROR: " + message;
					}
				}
			});
		}

		this.updatedocument = function(doc) {
			let self = this;
			this.listcontainer = document.getElementById("id_document");
			this.listcontainerbody = document.getElementById("id_documentbody");

			this.listcontainerbody.innerHTML = ""; // Svuota il contenuto precedente

			let header = document.createElement("h1");
			header.innerText = "Contenuto documento " + doc.name;
			self.listcontainerbody.appendChild(header);

			header = document.createElement("h3");
			header.innerText = "Nome: ";
			self.listcontainerbody.appendChild(header);
			let row = document.createElement("span");
			row.innerText = doc.name;
			self.listcontainerbody.appendChild(row);

			header = document.createElement("h3");
			header.innerText = "Id: ";
			self.listcontainerbody.appendChild(header);
			row = document.createElement("span");
			row.innerText = doc.documentId;
			self.listcontainerbody.appendChild(row);

			header = document.createElement("h3");
			header.innerText = "Tipo: ";
			self.listcontainerbody.appendChild(header);
			row = document.createElement("span");
			row.innerText = doc.type;
			self.listcontainerbody.appendChild(row);

			header = document.createElement("h3");
			header.innerText = "Sommario: ";
			self.listcontainerbody.appendChild(header);
			row = document.createElement("span");
			row.innerText = doc.summary;
			self.listcontainerbody.appendChild(row);

			header = document.createElement("h3");
			header.innerText = "Data creazione: ";
			self.listcontainerbody.appendChild(header);
			row = document.createElement("span");
			row.innerText = doc.date;
			self.listcontainerbody.appendChild(row);

			header = document.createElement("h3");
			header.innerText = "Percorso: ";
			self.listcontainerbody.appendChild(header);
			row = document.createElement("span");
			row.innerText = doc.path;
			self.listcontainerbody.appendChild(row);

			header = document.createElement("h3");
			header.innerText = "Proprietario: ";
			self.listcontainerbody.appendChild(header);
			row = document.createElement("span");
			row.innerText = doc.owner;
			self.listcontainerbody.appendChild(row);

			row = document.createElement("tr");
			space = document.createElement("br");
			row.appendChild(space);
			let prevBtn = document.createElement("button");
			prevBtn.textContent = "Pagina precedente";
			prevBtn.classList.add("btn");
			prevBtn.addEventListener("click", function() {
				var id = localStorage.getItem('folderId');
				var name = localStorage.getItem('folderName');
				showSubfolder(id, name);
			});
			row.appendChild(prevBtn);
			self.listcontainerbody.appendChild(row);

			this.listcontainer.style.visibility = "visible";
		};
	}




	function addDocument(e) {
		if (e.target.tagName.toLowerCase() === 'button') {
			e.preventDefault(); // Prevent default button behavior
			let parentCell = e.target.closest("li");
			// Create input element for folder name
			let form1 = document.createElement("form");
			let nome = document.createElement('input');
			nome.type = 'text';
			nome.placeholder = 'Nome';
			form1.appendChild(nome);
			form1.style.display = "inline-block";

			let summary = document.createElement('input');
			summary.type = 'text';
			summary.placeholder = 'Sommario';
			form1.appendChild(summary);

			let tipo = document.createElement('input');
			tipo.type = 'text';
			tipo.placeholder = 'Tipo';
			form1.appendChild(tipo);

			let parentId = parentCell.id;
			form1.id = "id_form_document_" + parentId;
			let creaBtn = document.createElement('button');
			creaBtn.textContent = 'Crea';
			form1.appendChild(creaBtn);

			// Replace button with input element
			e.target.parentNode.insertBefore(form1, e.target.nextSibling);

			// Remove the button element
			e.target.parentNode.removeChild(e.target);

			// Register click event listener on the "Crea" button
			creaBtn.addEventListener("click", function(createEvent) {
				createEvent.preventDefault();
				let documentname = nome.value.trim();
				let sommario = summary.value.trim();
				let type = tipo.value.trim();
				let documentId = parentId;
				if (documentname != "" && sommario != "" && type != "" && documentId != null) {
					makeCall2("POST", `createDocument?documentId=${encodeURIComponent(documentId)}&name=${encodeURIComponent(documentname)}&type=${encodeURIComponent(type)}&summary=${encodeURIComponent(sommario)}`
						, null, function(req) {
							if (req.readyState === XMLHttpRequest.DONE) {
								let message = req.responseText;
								if (req.status === 200) {
									let addButton = document.createElement('button');
									addButton.classList.add('btn');
									addButton.textContent = 'Aggiungi documento';
									addButton.addEventListener("click", addDocument);
									form1.parentNode.replaceChild(addButton, form1);
								} else if (req.status === 403) {
									window.location.href = req.getResponseHeader("Location");
									window.sessionStorage.removeItem('username');
								} else {
									alertContainer.textContent = "ERROR: " + message;
									setTimeout(function() {
										alertContainer.textContent = "";
									}, 3000);
									let addButton = document.createElement('button');
									addButton.classList.add('btn');
									addButton.textContent = 'Aggiungi documento';
									addButton.addEventListener("click", addDocument);
									form1.parentNode.replaceChild(addButton, form1);
								}
							}
						});
				} else {
					alertContainer.textContent = "ERROR: missing values";
					setTimeout(function() {
						alertContainer.textContent = "";
					}, 3000);
					let addButton = document.createElement('button');
					addButton.classList.add('btn');
					addButton.textContent = 'Aggiungi documento';
					addButton.addEventListener("click", addDocument);
					form1.parentNode.replaceChild(addButton, form1);
				}
			});
		}
	}



	function addSubfolder(e) {
		// Verifica se l'elemento cliccato è un pulsante all'interno della tabella
		if (e.target.tagName.toLowerCase() === 'button') {
			e.preventDefault(); // Prevent default button behavior
			let parentCell = e.target.closest("li");
			// Create input element for folder name
			let form = document.createElement("form");
			let name = document.createElement('input');
			name.type = 'text';
			name.placeholder = 'Nome';
			form.appendChild(name);
			let parentId = parentCell.id;
			form.id = "id_form_subfolder_" + parentId;
			let createBtn = document.createElement('button');
			createBtn.textContent = 'Crea';
			form.appendChild(createBtn);
			form.style.display = "inline-block";

			// Replace button with input element
			e.target.parentNode.insertBefore(form, e.target.nextSibling);

			// Remove the button element
			e.target.parentNode.removeChild(e.target);

			// Register click event listener on the "Crea" button
			createBtn.addEventListener("click", function(createEvent) {
				createEvent.preventDefault();
				let foldername = name.value.trim();

				let folderId = parentId;
				// Make AJAX call to create subfolder
				if (foldername !== "" && folderId != null) {
					makeCall2("POST", `createFolder?folderId=${encodeURIComponent(folderId)}&name=${encodeURIComponent(foldername)}`
						, null, function(req) {
							if (req.readyState === XMLHttpRequest.DONE) {
								let message = req.responseText;
								if (req.status === 200) {
									// Refresh the folder list after successful creation
									foldersList.show();
								} else if (req.status === 403) {
									window.location.href = req.getResponseHeader("Location");
									window.sessionStorage.removeItem('username');
								} else {
									alertContainer.textContent = "ERROR: " + message;
									setTimeout(function() {
										alertContainer.textContent = "";
									}, 3000);
									let addButton = document.createElement('button');
									addButton.classList.add('btn');
									addButton.textContent = 'Aggiungi sottocartella';
									addButton.addEventListener("click", addSubfolder);
									form.parentNode.replaceChild(addButton, form);
								}
							}
						});
				} else {
					alertContainer.textContent = "ERROR: missing values";
					setTimeout(function() {
						alertContainer.textContent = "";
					}, 3000);
					let addButton = document.createElement('button');
					addButton.classList.add('btn');
					addButton.textContent = 'Aggiungi sottocartella';
					addButton.addEventListener("click", addSubfolder);
					form.parentNode.replaceChild(addButton, form);
				}
			});
		}
	}

	function moveDocument(folder_id, document_id, name) {
		let errorMessage = document.getElementById("id_alertsub");
		if (folder_id != null && document_id != null && name != "") {
			makeCall2("POST", `moveDocument?folderId=${encodeURIComponent(folder_id)}&documentId=${encodeURIComponent(document_id)}`, null, function(req) {
				if (req.readyState === 4) {
					if (req.status === 200) {
						showSubfolder(folder_id, name);
					} else {
						if (req.responseText.length === 0) {
							errorMessage.innerHTML = "An unexpected error occurred.";
							setTimeout(function() {
								errorMessage.textContent = "";
							}, 3000);
						} else {
							errorMessage.innerHTML = req.responseText;
						}
					}
				}
			});
		} else {
			errorMessage.innerHTML = "An unexpected error occurred.";
			setTimeout(function() {
				errorMessage.textContent = "";
			}, 3000);
		}
	}

	function allowDrop(ev) {
		ev.preventDefault();
	}

	function drag(ev) {
		ev.dataTransfer.setData("text", ev.target.id);
		ev.dataTransfer.setData("isFolder", ev.target.isFolder);
		let id = ev.target.closest("li").fid;
		if (ev.target.isFolder == false) {
			var element = document.getElementById("id_home");
			document.getElementById("id_primary").style.display = "none";
			element.style.display = "block";
			element.classList.add("right-align");
			document.getElementById("benvenuto").style.display = "none";
			highlightFolder(id);
		}
	}

	function highlightFolder(folderId) {
		var folderElement = document.getElementById(folderId);
		if (folderElement) {
			folderElement.classList.add('selected-folder');
			let tbody = document.querySelector("#id_content");
			tbody.addEventListener('dragend', function() {
				folderElement.classList.remove('selected-folder');
				document.getElementById("id_home").style.display = "none";
				document.getElementById("id_home").classList.remove("right-align");
				document.getElementById("id_primary").style.display = "block";
				document.getElementById("benvenuto").style.display = "block";
			});
			folderElement.ondragover = null;
		}
	}

	function dropFolder(ev) {
		ev.preventDefault();
		if (ev.dataTransfer.getData("isFolder") == "false") {
			let document_id = ev.dataTransfer.getData("text");
			let folder_id = ev.target.closest("li").id;
			let name = ev.target.closest("li").name;
			if (folder_id == null || document_id == null || name == "") {
				return false;
			}
			moveDocument(folder_id, document_id, name);
		} else {
			alertContainer.textContent = "Impossible to move a folder in a folder";
			setTimeout(function() {
				alertContainer.textContent = "";
			}, 3000);
		}
	}

	function dropBin(ev) {
		ev.preventDefault();
		let id = ev.dataTransfer.getData("text");
		let isFolder = ev.dataTransfer.getData("isFolder") === "true";
		if (id == null) {
			if (isFolder == true) {
				alertContainer.textContent == "ERROR: id is null";
			} else if (isFolder == false) {
				let error = document.getElementById("id_alertsub");
				error.textContent = "ERROR: id is null";
			}
		} else {
			showDeleteModal(id, isFolder);
		}
	}

	function showDeleteModal(id, isFolder) {
		// Crea un elemento div per l'overlay
		var overlay = document.createElement("div");
		overlay.classList.add("overlay");


		var modal = document.createElement("div");
		modal.classList.add("modal");


		var modalContent = document.createElement("div");
		modalContent.classList.add("modal-content");

		var message = document.createElement("p");
		message.textContent = "Sei sicuro di voler cancellare questo elemento?";
		modalContent.appendChild(message);


		var confirmButton = document.createElement("button");
		confirmButton.textContent = "Conferma";
		confirmButton.addEventListener("click", function() {
			deleteItem(id, isFolder);
			document.body.removeChild(overlay);
			document.body.removeChild(modal);
		});
		modalContent.appendChild(confirmButton);

		var cancelButton = document.createElement("button");
		cancelButton.textContent = "Annulla";
		cancelButton.addEventListener("click", function() {
			// Chiudi la finestra e rimuovi l'overlay
			document.body.removeChild(overlay);
			document.body.removeChild(modal);
		});
		modalContent.appendChild(cancelButton);

		// Aggiungi il contenuto
		modal.appendChild(modalContent);

		// Aggiungi l'overlay e la finestra
		document.body.appendChild(overlay);
		document.body.appendChild(modal);

		// Visualizza 
		overlay.style.display = "block";
		modal.style.display = "block";
	}

	function deleteItem(id, isFolder) {
		makeCall2("DELETE", `delete?id=${id}&isFolder=${isFolder}`, null, function(req) {
			if (req.readyState === 4) {
				if (req.status === 200) {
					if (isFolder == true) {
						foldersList.show();
					} else {
						document.getElementById(id).remove(); //rimuovi elemento
					}
				} else {
					if (req.responseText.length === 0) {
						alertContainer.innerHTML = "An unexpected error occurred.";
						setTimeout(function() {
							alertContainer.textContent = "";
						}, 3000);
					} else {
						alertContainer.innerHTML = req.responseText;
					}
				}
			}
		});
	}

}