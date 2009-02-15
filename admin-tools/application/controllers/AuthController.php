<?php
class AuthController extends Zend_Controller_Action {


	function init() {
		$this->initView();
		Zend_Loader::loadClass('User');
		$this->view->baseUrl = $this->_request->getBaseUrl();
	}

	function indexAction() {
		$this->_redirect('/');
	}

	function loginAction() {
		$this->view->message = '';
		$session = new Zend_Session_Namespace('loginAuth');
		if ($this->_request->isPost()) {
			// collect the data from the user
			Zend_Loader::loadClass('Zend_Filter_StripTags');
			$f = new Zend_Filter_StripTags();
			$username = $f->filter($this->_request->getPost('username'));
			$session->username = $username;
			$password = $f->filter($this->_request->getPost('password'));

			if (empty($username)) {
				$this->view->message = 'Please provide a username.';
			} else {
				// setup Zend_Auth adapter for a database table
				Zend_Loader::loadClass('Zend_Auth_Adapter_DbTable');
				$db = Zend_Registry::get('db');

				$authAdapter = new Zend_Auth_Adapter_DbTable($db);
				$authAdapter->setTableName('user');
				$authAdapter->setIdentityColumn('username');
				$authAdapter->setCredentialColumn('password');
				// Set the input credential values to authenticate against
				$authAdapter->setIdentity($username);
				$authAdapter->setCredentialTreatment('password(?)');
				$authAdapter->setCredential($password);				

				// do the authentication
				$auth = Zend_Auth::getInstance();
				$result = $auth->authenticate($authAdapter);
				
				if ($result->isValid()) {
					// verify if the user have access to the tool
					$user = new User();

					$select = $user->select();
					$select->from($user, 'question_group_id')
						   ->where('username="' . $username . '"');
					
					$rowU = $user->fetchAll($select);
										
					$rowsetArray = $rowU->toArray();
					foreach ($rowsetArray as $rowArray) {
						foreach ($rowArray as $column => $value) {
							$question_group_id_value = $value;
						}
					
					}

					if ($question_group_id_value != 1) {	// not public
					
						// success: store database row to auth's storage
						// system. (Not the password though!)
						$data = $authAdapter->getResultRowObject(null, 'password');
						$auth->getStorage()->write($data);
						$this->_redirect('/');
					} else {
						// failure: access denied for this user
						$this->view->message = 'Access denied.';
					}
				} else {
					// failure: clear database row from session
					$this->view->message = 'Login failed.';
				}
				
			}
		}

		$this->view->title = "Log in";


	}

	function logoutAction() {
		Zend_Auth::getInstance()->clearIdentity();
		$this->_redirect('/');
	}


}