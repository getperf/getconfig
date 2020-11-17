package com.getconfig.Document

import com.getconfig.Model.RedmineParameter
import com.getconfig.Model.Ticket
import com.getconfig.Utils.TomlUtils
import com.taskadapter.redmineapi.Include
import com.taskadapter.redmineapi.IssueManager
import com.taskadapter.redmineapi.NotFoundException
import com.taskadapter.redmineapi.ProjectManager
import com.taskadapter.redmineapi.RedmineManager
import com.taskadapter.redmineapi.RedmineManagerFactory
import com.taskadapter.redmineapi.RedmineProcessingException
import com.taskadapter.redmineapi.bean.Issue
import com.taskadapter.redmineapi.bean.IssueFactory
import com.taskadapter.redmineapi.bean.Project
import com.getconfig.ConfigEnv
import com.getconfig.Controller
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

// setenv REDMINE_URL=http://localhost/redmine
// setenv REDMINE_API_KEY={APIキー}
// gradle --daemon test --tests "TestRedmineRepository.*"

/*
Redmine Java API 調査

* チケット作成の場合

  def new_issue = issue_manager.createIssue(issue); // 引数をリファレンスに発番

* チケット更新の場合

  issue_manager.update(issue); // issue を更新

更新の場合は名前でカスタムフィールドの指定ができるが、
作成の場合はカスタムフィールドID、名前、値の指定で登録が必要。

* カスタムフィールドの検索(Redmine 全体)

  def custom_fields = manager.getCustomFieldManager().getCustomFieldDefinitions()

* チケットの検索

  def params = new HashMap<String,String>();
  params.put("status_id","*");
  params.put("subject", subject);
  def results = issue_manager.getIssues(params).getResults()

* チケットのカスタムフィールド更新(新規チケットの場合)

  かなり複雑。一旦カスタムフィールドなしのチケットを作成してから次の更新をした方が良い

  # 事前にカスタムフィールドの辞書作成
  def custom_field_ids = [:]
  def custom_fields = manager.getCustomFieldManager().getCustomFieldDefinitions()
  custom_fields.each { custom_field ->
      custom_field_ids[custom_field.name] = custom_field.id
  }
  # 辞書からフィールドidをつき合わせし、カスタムフィールドを登録
  issue.addCustomField(CustomFieldFactory.create(custom_field_ids['OS名'], 'OS名', 'CentOS 6.10'))
  def new_issue = issue_manager.createIssue(issue);

* チケットのカスタムフィールド更新(既存チケットの場合)

  def custom_field = issue.getCustomFieldByName("OS名")
  custom_field.setValue('CentOS 6.9')
  issue_manager.update(issue);

* チケット削除

  mgr.getIssueManager().deleteIssue(0)

* リレーション登録

  def relations =[]
  def relation = issue_manager.createRelation(new_issue.id, issue_to.id, 'relates')
  relations << relation
  new_issue.addRelations(relations)
  issue_manager.update(new_issue)

* リレーション検索

  def relations = issue2.getRelations()

*/

@TypeChecked
@CompileStatic
@Slf4j
class TicketManager implements Controller {
    String redmineConfigPath = 'lib/dictionary/redmine.toml'
    String redmineUrl
    String redmineApiKey
    String inventoryField
    String redmineProject
    String rackLocationField
    String rackLocationFieldPrefix
    String trackerPortList
    int inOperationStatusId
    LinkedHashMap<String,String> portListCustomFields = [:]
    RedmineManager redmineManager
    IssueManager issueManager
    ProjectManager projectManager

    TicketManager(String redmineConfigPath = null) {
        this.redmineConfigPath = redmineConfigPath
    }

    void setEnvironment(ConfigEnv env) {
        this.redmineUrl = env.getRedmineUrl()
        this.redmineApiKey = env.getRedmineApiKey()
        this.redmineConfigPath = env.getRedmineConfigPath()
        this.redmineProject = env.getRedmineProject()
    }

    void readConfig() throws IOException {
        RedmineParameter config = TomlUtils.read(
            this.redmineConfigPath, RedmineParameter)as RedmineParameter
        config.setTicketManager(this)
    }

    def init() {
        if (!this.redmineManager) {
            this.redmineManager = RedmineManagerFactory.createWithApiKey(
                    this.redmineUrl,
                    this.redmineApiKey)
        }
        if (!this.issueManager) {
            this.issueManager   = this.redmineManager.getIssueManager()
        }
        if (!this.projectManager) {
            this.projectManager = this.redmineManager.getProjectManager()
        }
    }

    Issue getIssue(String subject) {
        def params = new HashMap<String,String>();
        params.put("status_id","*");
        params.put("subject", subject);
        def results = this.issueManager.getIssues(params).getResults()
        // getIssues()だと、リレーションの検索が出来ないため、再度getIssueById()で検索する
        return (results.isEmpty()) ? null :
                this.issueManager.getIssueById(results[0].id,
                        Include.relations)
    }

    Project getProject(String projectName) {
        try {
            return this.projectManager.getProjectByKey(projectName)
        } catch (NotFoundException e) {
            def msg = "Not found Redmine project '${projectName}' : ${e}"
            log.error(msg)
            throw new NotFoundException(msg)
        }
    }

    def delete(String subject) {
        def params = new HashMap<String,String>();
        params.put("status_id","*");
        params.put("subject", subject);

        def issues = this.issueManager.getIssues(params);
        issues.getResults().each { issue ->
            this.issueManager.deleteIssue(issue.id)
        }
    }

    Issue register(String projectName, String trackerName, String subject,
                   Map<String,String> customFields = null, Boolean in_operation = false) {
        Issue issue = null
        try {
            def project = this.getProject(projectName)
            def tracker = project.getTrackerByName(trackerName)
            if (!tracker) {
                def msg = "Not found Redmine tracker '${trackerName}' in '${projectName}'"
                log.error(msg)
                throw new NotFoundException(msg)
            }
            issue = getIssue(subject)
            if (!issue) {
                def new_issue = IssueFactory.create(null)
                new_issue.setProjectId(project.id)  // プロジェクト
                new_issue.setSubject(subject)       // 題名
                new_issue.setTracker(tracker)       // トラッカー
                issue = this.issueManager.createIssue(new_issue)
            }
            issue.setProjectId(project.id)
            issue.setSubject(subject)
            issue.setTracker(tracker)
            if (in_operation) {
                issue.setStatusId(this.inOperationStatusId)
            }
            updateCustomFields(issue, customFields)
            // log.info "Regist '${tracker_name}:${subject}(${project_name})'"
        } catch (NotFoundException e) {
            def msg = "Ticket regist failed '${trackerName}:${subject}'."
            log.error(msg)
            issue = null
        }
        return issue
    }

    def updateCustomFields(Issue issue, Map<String, String> customFields = null) {
        def trackerName = issue.getTracker().getName()
        def projectName = issue.getProjectName()
        def subject  = issue.getSubject()
        customFields.each { String fieldName, String value ->
            // println "UPDATE_CUSTOM_FIELDS:${field_name}, ${value}, ${value.getClass()}"
            def customField = issue.getCustomFieldByName(fieldName)
            if (!customField) {
                def msg = "Not found Redmine custom field '${fieldName}' in '${trackerName}'"
                log.error(msg)
                throw new NotFoundException(msg)
            }
            customField.setValue(value)
        }
        def customFieldInventory = issue.getCustomFieldByName(this.inventoryField)
        if (customFieldInventory) {
            customFieldInventory.setValue(subject)
        }
        def customFieldRackLocation = issue.getCustomFieldByName(this.rackLocationField)
        if (customFieldRackLocation) {
            def value = subject
            if (this.rackLocationFieldPrefix) {
                value = this.rackLocationFieldPrefix + subject
            }
            customFieldRackLocation.setValue(value)
        }
        try {
            this.issueManager.update(issue)
        } catch (RedmineProcessingException e) {
            def msg = "Redmine update error '${trackerName}:${subject}' set to '${customFields}' : ${e}."
            log.error(msg)
            throw new NotFoundException(msg)
        }
        log.info "Regist '${trackerName}:${subject}'"
    }

    Map<String,String> getPortListCustomFields(Map<String, String> customFields) {
        Map<String,String> fields = new LinkedHashMap<>()
        this.portListCustomFields.each { redmineName, testItemName ->
            if (customFields.containsKey(testItemName)) {
                def value = customFields[testItemName]
                // println "PORT_LIST_CUSTOM_FIELDS:${test_item_name},${value}"
                if (value != null) {
                    fields[redmineName] = value
                }
            }
        }
        return fields
    }

    Boolean checkLookupedPortList(Map<String, String> customFields) {
        return (customFields.containsKey('lookup') && customFields['lookup'] == true)
    }

    Issue regist_port_list(String projectName, String subject, Map customFields = [:]) {
        // println "CUSTOM_FIELDS:${custom_fields}"
        // println "PORT_LIST_CUSTOM_FIELDS:${this.port_list_custom_fields}"
        // println "TRACKER_PORT_LIST: ${this.tracker_port_list}"
        if (this.portListCustomFields.size() == 0) {
            def msg = "Redmine update error '${subject}' : Not found 'port_list_custom_fields'"
            log.error(msg)
            throw new NotFoundException(msg)
        }
        def fields = this.getPortListCustomFields(customFields)
        def lookuped = this.checkLookupedPortList(customFields)
        return this.register(projectName, this.trackerPortList, subject, fields, lookuped)
    }

    Boolean link(Issue ticket_from, List<Integer> ticket_to_ids) {
        Boolean isok = false
        // 関連するチケットIDの洗い出し
        def existing_relations = [:]
        ticket_from.getRelations().each { issue ->
            existing_relations[issue.issueId] = true
            existing_relations[issue.issueToId] = true
        }
        // println "EXISTING_RELATIONS:${existing_relations}"
        def relations =[]
        try {
            ticket_to_ids.each { ticket_to_id ->
                if (!existing_relations.containsKey(ticket_to_id)) {
                    relations << this.issueManager.createRelation(ticket_from.id,
                            ticket_to_id,
                            'relates')
                }
            }
            // println "RELATIONS:${relations}"
            if (relations.size() > 0) {
                ticket_from.addRelations(relations)
                this.issueManager.update(ticket_from)
            }
            isok = true
        } catch (RedmineProcessingException e) {
            def msg = "Redmine link error '${ticket_from}' to '${ticket_to_ids}' : ${e}."
            log.info(msg)
        }
        return isok
    }

    int run() {
        return 0
    }

    void resister(Ticket ticket) {
        println "RESISTER:${this.redmineProject},$ticket"
        Issue issue = register(this.redmineProject, ticket.tracker,
            ticket.subject, ticket.custom_fields)
        println "ID:${issue.id}"
    }
}
